package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.BuyOrder;
import pkg.order.Order;
import pkg.order.OrderType;
import pkg.order.SellOrder;
import pkg.stock.Stock;

public class Trader {
	// Name of the trader
	String name;
	// Cash left in the trader's hand
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> position;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;

	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
		// Buy stock straight from the bank
		// Need not place the stock in the order list
		// Add it straight to the user's position
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// Adjust cash possessed since the trader spent money to purchase a
		// stock.
		
		Stock stock = m.getStockForSymbol(symbol);
		if (stock != null) {
			double price = stock.getPrice();
			double cost = price * volume;
			
			if (cost > cashInHand) {
				throw new StockMarketExpection("Cannot place order for stock: " + symbol + " since there is not enough money. Trader: " + name);
			} else {
				cashInHand -= cost;
				
				BuyOrder order = new BuyOrder(symbol, volume, price, this);
				position.add(order);
			}
		}
	}

	public void placeNewOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Place a new order and add to the orderlist
		// Also enter the order into the orderbook of the market.
		// Note that no trade has been made yet. The order is in suspension
		// until a trade is triggered.
		//
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// A trader cannot place two orders for the same stock, throw an
		// exception if there are multiple orders for the same stock.
		// Also a person cannot place a sell order for a stock that he does not
		// own. Or he cannot sell more stocks than he possesses. Throw an
		// exception in these cases.
		
		if (orderType == OrderType.BUY) {
			BuyOrder order = new BuyOrder(symbol, volume, price, this);
			
			for (Order o : ordersPlaced) {
				if (o.getStockSymbol() == order.getStockSymbol()) {
					throw new StockMarketExpection("An order for this stock has already been placed. Stock: " + symbol + " Trader: " + name);
				}
			}
			
			
			ordersPlaced.add(order);
			m.addOrder(order);
		} else {
			SellOrder order = new SellOrder(symbol, volume, price, this);
			
			boolean found = false;
			for (Order o : position) {
				if (o.getStockSymbol() == order.getStockSymbol()) {
					found = true;
					
					if (order.getSize() > o.getSize()) {
						throw new StockMarketExpection("The trader does not have enough stock to sell. Stock: " + symbol + " Trader: " + name);
					}
				}
			}
			for (Order o : ordersPlaced) {
				if (o.getStockSymbol() == order.getStockSymbol()) {
					throw new StockMarketExpection("An order for this stock has already been placed. Stock: " + symbol + " Trader: " + name);
				}
			}
			
			if (found == false) {
				throw new StockMarketExpection("The trader does not have the stock for this order. Stock: " + symbol + " Trader: " + name);
			}
			
			ordersPlaced.add(order);
			m.addOrder(order);
		}

	}

	public void placeNewMarketOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		// Similar to the other method, except the order is a market order
		
		if (orderType == OrderType.BUY) {
			BuyOrder order = new BuyOrder(symbol, volume, true, this);
			
			for (Order o : ordersPlaced) {
				if (o.getStockSymbol() == order.getStockSymbol()) {
					throw new StockMarketExpection("An order for this stock has already been placed. Stock: " + symbol + " Trader: " + name);
				}
			}
			
			
			ordersPlaced.add(order);
			m.addOrder(order);
		} else {
			SellOrder order = new SellOrder(symbol, volume, true, this);
			
			boolean found = false;
			for (Order o : position) {
				if (o.getStockSymbol() == order.getStockSymbol()) {
					found = true;
					
					if (order.getSize() > o.getSize()) {
						throw new StockMarketExpection("The trader does not have enough stock to sell. Stock: " + symbol + " Trader: " + name);
					}
				}
			}
			for (Order o : ordersPlaced) {
				if (o.getStockSymbol() == order.getStockSymbol()) {
					throw new StockMarketExpection("An order for this stock has already been placed. Stock: " + symbol + " Trader: " + name);
				}
			}
			
			if (found == false) {
				throw new StockMarketExpection("The trader does not have the stock for this order. Stock: " + symbol + " Trader: " + name);
			}
			
			ordersPlaced.add(order);
			m.addOrder(order);
		}
	}

	public void tradePerformed(Order o, double matchPrice)
			throws StockMarketExpection {
		// Notification received that a trade has been made, the parameters are
		// the order corresponding to the trade, and the match price calculated
		// in the order book. Note than an order can sell some of the stocks he
		// bought, etc. Or add more stocks of a kind to his position. Handle
		// these situations.

		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
		
		OrderType type = o.getOrderType();
		
		if (type == OrderType.BUY) {
			cashInHand -= o.getSize() * matchPrice;
			ordersPlaced.remove(o);
			o.setPrice(matchPrice);
			boolean found = false;
			for (Order k : position) {
				if (k.getStockSymbol() == o.getStockSymbol()) {
					found = true;
					k.setPrice(matchPrice);
					k.setSize(k.getSize() + o.getSize());
				}
			}
			if (found == false) {
				position.add(o);
			}
		} else {
			cashInHand += o.getSize() * matchPrice;
			ordersPlaced.remove(o);
			boolean remove = false;
			int idx = 0;
			Order rem = null;
			for (Order k : position) {
				if (k.getStockSymbol() == o.getStockSymbol()) {
					if (k.getSize() > o.getSize()) {
						k.setSize(k.getSize() - o.getSize());
					} else {
						remove = true;
						rem = k;
					}
				}
				idx++;
			}
			if (remove == true) {
				position.remove(rem);
			}
		}
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order o : position) {
			o.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order o : ordersPlaced) {
			o.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
