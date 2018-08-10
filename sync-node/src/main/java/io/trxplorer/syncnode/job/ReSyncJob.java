package io.trxplorer.syncnode.job;

import static io.trxplorer.model.Tables.*;

import java.util.List;

import org.jooby.quartz.Scheduled;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.trxplorer.syncnode.SyncNodeConfig;
import io.trxplorer.syncnode.service.BlockService;
import io.trxplorer.syncnode.service.ServiceException;
import io.trxplorer.troncli.TronFullNodeCli;

@Singleton
@DisallowConcurrentExecution
public class ReSyncJob {


	private DSLContext dslContext;
	private BlockService blockService;
	private TronFullNodeCli fullNodeCli;
	private SyncNodeConfig config;
	private static final Logger logger = LoggerFactory.getLogger(ReSyncJob.class);

	@Inject
	public ReSyncJob(DSLContext dslContext,BlockService blockService,TronFullNodeCli fullNodeCli,SyncNodeConfig config) {
		this.dslContext = dslContext;
		this.blockService = blockService;
		this.fullNodeCli = fullNodeCli;
		this.config = config;
	}
	
	@Scheduled("1m")
	public void reSyncBlocks() throws ServiceException {
		
		if (!this.config.isResyncJobEnabled()) {
			return;
		}
		
		List<ULong> blocks = this.dslContext.select(BLOCK_RESYNC.NUM)
		.from(BLOCK_RESYNC)
		.limit(500)
		.fetchInto(ULong.class);

		for(ULong blockNum:blocks) {
			
			logger.info("[RESYNC] => Resyncing block:"+blockNum);
			
			this.dslContext.deleteFrom(BLOCK).where(BLOCK.NUM.eq(blockNum)).execute();
			this.blockService.importBlock(this.fullNodeCli.getBlockByNum(blockNum.longValue()));
			this.blockService.confirmBlock(blockNum.longValue());
			this.dslContext.deleteFrom(BLOCK_RESYNC).where(BLOCK_RESYNC.NUM.eq(blockNum)).execute();
		}
		
		
	}
	

}
