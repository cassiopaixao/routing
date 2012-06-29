package routing;

import java.util.Map;

import core.Connection;
import core.DTNHost;
import core.Settings;
import core.SimClock;

public class FreshRouter extends ActiveRouter {

	private Map<DTNHost, Double> encounterAge;

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

	/*
	 * Retorna há quanto tempo o host encontrou o host <code>peer</code>. Se
	 * nunca se encontraram, retorna infinito (Double.MAX_VALUE).
	 */
	private double prevEncounterAge(DTNHost peer) {

		if (encounterAge.containsKey(peer)) {
			Double prevEncAge = encounterAge.get(peer);
			return SimClock.getTime() - prevEncAge.doubleValue();
		} else {
			return Double.MAX_VALUE;
		}

	}

	/*
	 * Encontra um host que esteve em contato com o destino há menos tempo.
	 * Se não houver nenhum, retorna <code>null</code>.
	 */
	private DTNHost findNextAnchor(DTNHost dest, double time) {
		DTNHost anchor = null;
		double maisRecente = Double.MAX_VALUE;
		for (Connection con : getConnections()) {
			double tempo = prevEncounterAge(con.getOtherNode(this.getHost()));
			if (tempo <= maisRecente) {
				anchor = con.getOtherNode(this.getHost());
			}
		}
		
		return anchor;
	}

	@Override
	public void changedConnection(Connection con) {
		super.changedConnection(con);
		
		DTNHost peer = con.getOtherNode(this.getHost());
		encounterAge.put(peer, SimClock.getTime());
	}

	@Override
	/*
	 * (non-Javadoc) Elimina do buffer as mensagens transferidas.
	 * @see routing.ActiveRouter#transferDone(core.Connection)
	 */
	protected void transferDone(Connection con) {
		/* don't leave a copy for the sender */
		this.deleteMessage(con.getMessage().getId(), false);
	}
	
}
