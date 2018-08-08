package io.trxplorer.troncli;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.BlockLimit;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.Node;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.api.WalletGrpc;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Witness;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.typesafe.config.Config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.trxplorer.troncli.wallet.BroadcastResult;

public class TronFullNodeCli {
	
	private ManagedChannel channelFull = null;
	private WalletGrpc.WalletBlockingStub client = null;
	
	public static final byte ADD_PRE_FIX_BYTE_MAINNET = (byte) 0x41;   //41 + address
	   
	public static final byte ADD_PRE_FIX_BYTE_TESTNET = (byte) 0xa0;   //a0 + address
	
	private static final Logger logger = LoggerFactory.getLogger(TronFullNodeCli.class);
	
	@Inject
	public TronFullNodeCli(Config config) {
		this(config.getString("tron.fullnode"),config.getBoolean("tron.mainNet"));
	}

	public TronFullNodeCli(String fullNodeAddress,boolean mainNet) {
		
		if (mainNet) {
			Wallet.setAddressPreFixByte(Constant.ADD_PRE_FIX_BYTE_MAINNET);	
		}
		
		
		channelFull = ManagedChannelBuilder.forTarget(fullNodeAddress)
	              .usePlaintext(true)
	              .build();
		
		this.client = WalletGrpc.newBlockingStub(channelFull);
	}

	
	public Account getAccountByAddress(String address) {
		try {
		ByteString addressBS = ByteString.copyFrom(Wallet.decodeFromBase58Check(address));
		
		Account request = Account.newBuilder().setAddress(addressBS).build();
		
		Account account = this.client.getAccount(request);

		return account;
		}catch(Exception e) {
			logger.error("Could not get account:"+address, e);
		}
		return null;
	}

	
	public Block getBlockByNum(Long blockNum) {

		return this.client.getBlockByNum(NumberMessage.newBuilder().setNum(blockNum).build());
	}

	
	public List<Block> getBlocks(long start,long stop) {
		
		BlockList blockByLimitNext = this.client.getBlockByLimitNext(BlockLimit.newBuilder().setStartNum(start).setEndNum(stop).build());
		
		return blockByLimitNext.getBlockList();
	}
	
	public List<Witness> getAllWitnesses() {

		List<Witness> result = new ArrayList<>();

		WitnessList witnessList = this.client.listWitnesses(EmptyMessage.newBuilder().build());
		
		if (witnessList!=null) {
			result = witnessList.getWitnessesList();
		}

		return result;
	}
	
	
	public List<Node> getAllNodes() {
		
		List<Node> result = new ArrayList<>();

		NodeList nodeList = this.client.listNodes(EmptyMessage.newBuilder().build());
		
		if (nodeList!=null) {
			result = nodeList.getNodesList();
		}
		
		return result;
	}
	
	public long getNextMaintenanceTime() {
		
		NumberMessage res = this.client.getNextMaintenanceTime(EmptyMessage.newBuilder().build());

		if (res==null) {
			return -1;
		}
		
		return res.getNum();
	}
	
	public Block getLastBlock() {
		return client.getNowBlock(EmptyMessage.newBuilder().build());
	}


	public BroadcastResult broadcastTransaction(byte[] bytes) {
		
		BroadcastResult result = new BroadcastResult();		
		try {

			Transaction transaction = Transaction.parseFrom(bytes);
			
			Return bReturn = this.client.broadcastTransaction(transaction);
			
			result.setSuccess(bReturn.getResult());
			result.setErrorMsg(bReturn.getMessage().toStringUtf8());
			result.setCode(bReturn.getCode().getNumber());
			result.setTxId(Sha256Hash.of(transaction.getRawData().toByteArray()).toString());
			
			
			return result;
			
		} catch (InvalidProtocolBufferException e) {
			result.setSuccess(false);
			result.setErrorMsg("Could not parse transaction");
		}

		
		
		return result;
	}
	
	public void shutdown() throws InterruptedException {
		this.channelFull.shutdown();
		this.channelFull.awaitTermination(10, TimeUnit.SECONDS);

	}



}
