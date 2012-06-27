package routing;

import core.Settings;

public class FreshRouter extends ActiveRouter {

	public FreshRouter(Settings s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	protected FreshRouter(FreshRouter r) {
		super(r);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FreshRouter replicate() {
		return new FreshRouter(this);
	}

}
