package io.trxplorer.syncnode.service;

import static io.trxplorer.model.Tables.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.jooq.types.UByte;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Account.Frozen;
import org.tron.protos.Protocol.Vote;

import com.google.inject.Inject;

import io.trxplorer.model.tables.AccountVote;
import io.trxplorer.model.tables.records.AccountAssetRecord;
import io.trxplorer.model.tables.records.AccountRecord;
import io.trxplorer.model.tables.records.AccountVoteRecord;
import io.trxplorer.troncli.TronFullNodeCli;

public class AccountService {

	private DSLContext dslContext;
	
	private TronFullNodeCli tronFullNodeCli;
	
	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
	
	@Inject
	public AccountService(DSLContext dslContext,TronFullNodeCli tronFullNodeCli) {
		this.dslContext = dslContext;
		this.tronFullNodeCli = tronFullNodeCli;
	}
	
	
	
	public void createOrUpdateAccount(String address) throws ServiceException {
		
		Account tronAccount = this.tronFullNodeCli.getAccountByAddress(address);
		
		if (tronAccount==null) {
			logger.error(String.format("Could not find tron account with address: %s", address));
			return;
		}
		
		// Try to fetch existing account
		AccountRecord record = this.dslContext.select(ACCOUNT.ID)
		.from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetchOneInto(AccountRecord.class);
		
		// Create it if it doesn't exists yet
		if (record==null) {

			record = this.dslContext.insertInto(ACCOUNT)
			.set(ACCOUNT.ACCOUNT_NAME,tronAccount.getAccountName().toStringUtf8())
			.set(ACCOUNT.TYPE,(byte) tronAccount.getType().getNumber())
			.set(ACCOUNT.IS_WITNESS,(tronAccount.getIsWitness()==true ? UByte.valueOf((byte)1) : UByte.valueOf((byte)0)))
			.set(ACCOUNT.IS_COMMITTEE,(tronAccount.getIsCommittee()==true ? UByte.valueOf((byte)1) : UByte.valueOf((byte)0)))
			.set(ACCOUNT.ADDRESS,address)
			.set(ACCOUNT.ALLOWANCE, ULong.valueOf(tronAccount.getAllowance()))
			.set(ACCOUNT.CREATE_TIME,new Timestamp(tronAccount.getCreateTime()))
			.set(ACCOUNT.BALANCE,tronAccount.getBalance())
			.set(ACCOUNT.BANDWIDTH,tronAccount.getNetUsage())
			.returning(ACCOUNT.ID)
			.fetchOne();
			
		}else {
			
			//Update if exists
			this.dslContext.update(ACCOUNT)
			.set(ACCOUNT.ACCOUNT_NAME,tronAccount.getAccountName().toStringUtf8())
			.set(ACCOUNT.TYPE,(byte) tronAccount.getType().getNumber())
			.set(ACCOUNT.BALANCE,tronAccount.getBalance())
			.set(ACCOUNT.CREATE_TIME,new Timestamp(tronAccount.getCreateTime()))
			.set(ACCOUNT.ALLOWANCE, ULong.valueOf(tronAccount.getAllowance()))
			.set(ACCOUNT.BALANCE,tronAccount.getBalance())
			.set(ACCOUNT.BANDWIDTH,tronAccount.getNetUsage())
			.set(ACCOUNT.IS_WITNESS,(tronAccount.getIsWitness()==true ? UByte.valueOf((byte)1) : UByte.valueOf((byte)0)))
			.set(ACCOUNT.IS_COMMITTEE,(tronAccount.getIsCommittee()==true ? UByte.valueOf((byte)1) : UByte.valueOf((byte)0)))
			.set(ACCOUNT.TRANSFER_TO_COUNT,DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.TO.eq(address)))
			.set(ACCOUNT.TRANSFER_FROM_COUNT,DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.FROM.eq(address)))
			.set(ACCOUNT.TOKENS_COUNT,DSL.select(DSL.count()).from(ACCOUNT_ASSET).where(ACCOUNT_ASSET.ACCOUNT_ID.eq(record.getId())))
			.set(ACCOUNT.PARTICIPATIONS_COUNT,DSL.select(DSL.count()).from(CONTRACT_PARTICIPATE_ASSET_ISSUE).where(CONTRACT_PARTICIPATE_ASSET_ISSUE.OWNER_ADDRESS.eq(address)))
			.where(ACCOUNT.ID.eq(record.getId()))
			.execute();
			
			
		}
		
		
		// Update account assets
		if (tronAccount.getAssetMap()!=null && tronAccount.getAssetMap().size()>0) {

			this.dslContext.delete(ACCOUNT_ASSET).where(ACCOUNT_ASSET.ACCOUNT_ID.eq(record.getId())).execute();
			
			//List<AccountAssetRecord> accountAssetRecords = new ArrayList<>();
			
			List<Query> queries = new ArrayList<>();
			
			for(String assetName:tronAccount.getAssetMap().keySet()) {
				
//				AccountAssetRecord assetRecord = new AccountAssetRecord();
//				assetRecord.setAssetName(assetName);
//				assetRecord.setBalance(ULong.valueOf(tronAccount.getAssetMap().get(assetName)));
//				assetRecord.setAccountId(record.getId());
//				
//				accountAssetRecords.add(assetRecord);
				
				queries.add(DSL.insertInto(ACCOUNT_ASSET)
						.set(ACCOUNT_ASSET.ASSET_NAME,assetName)
						.set(ACCOUNT_ASSET.BALANCE,ULong.valueOf(tronAccount.getAssetMap().get(assetName)))
						.set(ACCOUNT_ASSET.ACCOUNT_ID,record.getId())
						.onDuplicateKeyIgnore());
				
			}
			
			this.dslContext.batch(queries).execute();
			//this.dslContext.batchInsert(accountAssetRecords).execute();
			
		}
		
		this.dslContext.delete(ACCOUNT_VOTE).where(ACCOUNT_VOTE.ACCOUNT_ID.eq(record.getId())).execute();
		
		// Update account votes (= current votes)
		if (tronAccount.getVotesList()!=null && tronAccount.getVotesList().size()>0) {
			
			
			//List<AccountVoteRecord> accountVoteRecords = new ArrayList<>();
			List<Query> queries = new ArrayList<>();
			
			for(Vote accountVote:tronAccount.getVotesList()) {
				
//				AccountVoteRecord voteRecord = new AccountVoteRecord();
//				voteRecord.setVoteAddress(Wallet.encode58Check(accountVote.getVoteAddress().toByteArray()));
//				voteRecord.setVoteCount(ULong.valueOf(accountVote.getVoteCount()));
//				voteRecord.setAccountId(record.getId());
				//voteRecord.setTimestamp();
				//accountVoteRecords.add(voteRecord);
				queries.add(DSL.insertInto(ACCOUNT_VOTE)
						.set(ACCOUNT_VOTE.VOTE_ADDRESS,Wallet.encode58Check(accountVote.getVoteAddress().toByteArray()))
						.set(ACCOUNT_VOTE.VOTE_COUNT,ULong.valueOf(accountVote.getVoteCount()))
						.set(ACCOUNT_VOTE.ACCOUNT_ID,record.getId())
						.onDuplicateKeyIgnore());
				
			}
			
			this.dslContext.batch(queries).execute();
			
			//update vote timestamp
			AccountVote AccountVote = ACCOUNT_VOTE.as("av");
			
			
			
//			SelectConditionStep<Record1<Timestamp>> voteTimestamp = DSL.select(DSL.max(BLOCK.TIMESTAMP))
//			.from(CONTRACT_VOTE_WITNESS)
//			.join(TRANSACTION).on(TRANSACTION.ID.eq(CONTRACT_VOTE_WITNESS.TRANSACTION_ID))
//			.join(BLOCK).on(TRANSACTION.BLOCK_ID.eq(BLOCK.ID))
//			.where(CONTRACT_VOTE_WITNESS.OWNER_ADDRESS.eq(address))
//			.and(CONTRACT_VOTE_WITNESS.VOTE_COUNT.eq(AccountVote.VOTE_COUNT))
//			.and(CONTRACT_VOTE_WITNESS.VOTE_ADDRESS.eq(AccountVote.VOTE_ADDRESS));
//			
//			this.dslContext.update(AccountVote)
//			.set(AccountVote.TIMESTAMP,voteTimestamp)
//			.where(AccountVote.ACCOUNT_ID.eq(record.getId()))
//			.execute();
			
		}
		
		
		this.dslContext.delete(ACCOUNT_FROZEN).where(ACCOUNT_FROZEN.ACCOUNT_ID.eq(record.getId())).execute();
		
		// Update frozen balance
		if (tronAccount.getFrozenList()!=null && tronAccount.getFrozenList().size()>0) {
			
			List<Query> queries = new ArrayList<>();
			
			for(Frozen frozen:tronAccount.getFrozenList()) {
				
//				this.dslContext.insertInto(ACCOUNT_FROZEN)
//				.set(ACCOUNT_FROZEN.EXPIRE_TIME, new Timestamp(frozen.getExpireTime()))
//				.set(ACCOUNT_FROZEN.BALANCE,frozen.getFrozenBalance())
//				.set(ACCOUNT_FROZEN.ACCOUNT_ID,record.getId())
//				.onDuplicateKeyIgnore()
//				.execute();
				
				queries.add(DSL.insertInto(ACCOUNT_FROZEN)
						.set(ACCOUNT_FROZEN.EXPIRE_TIME, new Timestamp(frozen.getExpireTime()))
						.set(ACCOUNT_FROZEN.BALANCE,frozen.getFrozenBalance())
						.set(ACCOUNT_FROZEN.ACCOUNT_ID,record.getId())
						.onDuplicateKeyIgnore());
				
			}
			
			this.dslContext.batch(queries).execute();
			

		}
		
		
		
	}
	
	

	
}
