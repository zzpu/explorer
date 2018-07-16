package io.trxplorer.api.route;

public interface ApiAppRoutePaths {

	// API
	public static final String API = "/api";
	
	public static interface V1{
		
		public static final String VERSION = "/v1";
		
		//ACCOUNT
		public static final String ACCOUNT = VERSION+"/account";
		public static final String ACCOUNT_INFO = ACCOUNT+"/info";
		public static final String ACCOUNT_EXISTS = ACCOUNT+"/exists";
		public static final String ACCOUNT_TOKENS = ACCOUNT+"/tokens";
		public static final String ACCOUNT_TRANSACTIONS = ACCOUNT+"/transactions";
		public static final String ACCOUNT_VOTES_ALL = ACCOUNT+"/votes/all";
		public static final String ACCOUNT_VOTES_NOW = ACCOUNT+"/votes/now";
		public static final String ACCOUNT_FREEZE_ALL = ACCOUNT+"/freeze/all";
		public static final String ACCOUNT_FREEZE_NOW = ACCOUNT+"/freeze/now";
		
		//WITNESS
		public static final String WITNESS = VERSION+"/witness";		
		public static final String WITNESS_ALL = WITNESS+"/all";
		
		//VOTES
		public static final String VOTE_ROUND = VERSION+"/vote/:round";
		public static final String VOTE_ROUND_STATS = VERSION+"/vote/stats/:maxRound";
		public static final String VOTE_ROUND_VOTES = VERSION+"/vote/:round/:address";
		public static final String VOTE_LIVE_VOTES = VERSION+"/vote/live/:address";
		public static final String VOTE_LIVE = VERSION+"/vote/live";		
		public static final String VOTE_LIVE_TOTAL = VERSION+"/vote/live/total";
		
		//TRON
		public static final String TRON = VERSION+"/tron";
		public static final String TRON_BROADCAST = TRON+"/broadcast";
		
		
		//BLOCK
		public static final String BLOCK = VERSION+"/block";
		public static final String BLOCK_LATEST = BLOCK+"/latest";



		
	}
	
}
