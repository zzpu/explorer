package io.trxplorer.troncli;

import org.junit.Ignore;
import org.junit.Test;

public class TronCliTest {

	
	@Test
	public void testFullNodeCli() {
		
		TronFullNodeCli cli = new TronFullNodeCli("3.225.171.164:50051",true);
		System.out.println(cli.getLastBlock().getBlockHeader().getRawData().getNumber());
		System.out.println(cli.getAllNodes().size());
		
		
	}
	
//	@Test
//	public void testSolidityNodeCli() {
//
//		TronSolidityNodeCli cli = new TronSolidityNodeCli("3.225.171.164:50051",true);
//
//		System.out.println(cli.getLastBlock().getBlockHeader().getRawData().getNumber());
//
//	}
	
}
