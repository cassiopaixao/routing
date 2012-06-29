package routing;

import java.util.HashMap;
import java.util.Map;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;

public class FreshRouter extends ActiveRouter {

	private Map<DTNHost, Double> encounterAge;

	public FreshRouter(Settings s) {
		super(s);
		initData();
	}

	protected FreshRouter(FreshRouter r) {
		super(r);
		initData();
	}

	private void initData() {
		encounterAge = new HashMap<DTNHost, Double>();
		encounterAge.put(getHost(), new Double(0.0));
	}

	@Override
	public FreshRouter replicate() {
		return new FreshRouter(this);
	}

	/*
	 * Retorna há quanto tempo o host encontrou o host <code>peer</code>.
	 * Se nunca se encontraram, retorna infinito (Double.MAX_VALUE).
	 * Se é o próprio host, retorna 0.0.
	 */
	private double prevEncounterAge(DTNHost peer) {

		if (encounterAge.containsKey(peer)) {
			Double prevEncAge = encounterAge.get(peer);
			if (peer.equals(getHost())) {
				return 0.0;
			}
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
		// TODO Verificar se não está deletando também no nó que recebeu a msg
		this.deleteMessage(con.getMessage().getId(), false);
	}

	@Override
	public void update() {
		super.update();
		if (!canStartTransfer() || isTransferring()) {
			return; // nothing to transfer or is currently transferring 
		}
		
		// try messages that could be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return;
		}
		
		tryOtherMessages();
	}

	private void tryOtherMessages() {
		for (Message m : this.getMessageCollection()) {
			Connection con = fresh(m);
		}
	}

	/*
	 * Tenta enviar uma mensagem para próxima âncora
	 */
	private Connection fresh(Message m) {
		if (m.getTo().equals(getHost())) {
			// TODO
			// replyToSource();
		}
		else {
			double t = prevEncounterAge(m.getTo());
			DTNHost a = findNextAnchor(m.getTo(), t);
			if (a != null && !m.getTo().equals(a)) {
				return notifyNextAnchor(a, m);
			}
		}
		return null;
	}

	/*
	 * Envia mensagem para o próximo nó.
	 */
	private Connection notifyNextAnchor(DTNHost a, Message m) {
		Connection con = getConnectionWith(a);
		int status = con.startTransfer(this.getHost(), m);
		if (status == RCV_OK) {
			return con;	// accepted a message, don't try others
		}
		return null; // transferência não foi iniciada.
	}
	
	private Connection getConnectionWith(DTNHost a) {
		for (Connection con : getConnections()) {
			if (con.getOtherNode(this.getHost()).equals(a)) {
				return con;
			}
		}
		return null;
	}
	
}
