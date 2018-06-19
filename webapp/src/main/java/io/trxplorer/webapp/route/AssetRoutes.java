package io.trxplorer.webapp.route;

import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Results;
import org.jooby.Route.Chain;
import org.jooby.View;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.trxplorer.webapp.dto.asset.AssetIssueDTO;
import io.trxplorer.webapp.dto.asset.AssetIssueDetailCriteriaDTO;
import io.trxplorer.webapp.dto.asset.AssetIssueListCriteriaDTO;
import io.trxplorer.webapp.service.AssetService;

@Singleton
public class AssetRoutes {

	private AssetService assetService;

	@Inject
	public AssetRoutes(AssetService blockService) {
		this.assetService = blockService;
	}
	
	@GET
	@Path(TRXPlorerRoutePaths.Front.ASSET_DETAIL)
	public void assetDetail(Request req,Response res,Chain chain) throws Throwable {

		Integer limit = req.param("limit").intValue(20);
		Integer page = req.param("page").intValue(1);
		
		String tab = req.param("t").value("tx");
		
		String assetName = req.param("assetName").value(null); 
		
		View view = Results.html("asset/asset.detail");
		
		AssetIssueDetailCriteriaDTO criteria = new AssetIssueDetailCriteriaDTO();
		criteria.setLimit(limit);
		criteria.setPage(page);
		criteria.setName(assetName);
		criteria.setTab(tab);

		AssetIssueDTO assetIssue = this.assetService.getAssetDetails(criteria);
		
		if (assetIssue==null) {
			chain.next(req, res);
		}else {
			view.put("asset",assetIssue);
			res.send(view);			
		}

	}
	
	@GET
	@Path(TRXPlorerRoutePaths.Front.ASSET_LIST)
	public void assetList(Request req,Response res) throws Throwable {
		
		Integer limit = req.param("limit").intValue(20);
		Integer page = req.param("page").intValue(1);
		

		
		AssetIssueListCriteriaDTO criteria = new AssetIssueListCriteriaDTO();
		

		criteria.setLimit(limit);
		criteria.setPage(page);

		View view = Results.html("asset/asset.list");
		
		view.put("list",this.assetService.listAssetIssues(criteria));
		
		res.send(view);
	}
	
	
	
}
