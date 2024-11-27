package com.akul.market.service;

import com.akul.market.json.search.SearchResult;
import com.akul.market.json.stat.Customer;
import com.akul.market.json.stat.Stat;
import com.akul.market.repository.BuyerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JsonService {

    private final BuyerService buyerService;

    public JsonService(BuyerService buyerService) {
        this.buyerService = buyerService;
    }

    public SearchResult searchJson(LinkedHashMap<String, List<Object>> searchCriteria) {
        return null;
    }

    public Stat statJson(Date dateFrom, Date dateTo) {
        Stat stat = new Stat();
        List<BuyerRepository.BuyerStatJSON> listBuyers = buyerService.buyersByDate(dateFrom, dateTo);
        Map<String, List<BuyerRepository.BuyerStatJSON>> map = new HashMap<>();
        for (BuyerRepository.BuyerStatJSON purchase : listBuyers) {

            List list;

            if (!map.containsKey(purchase.getBuyerName())) {
                list = new ArrayList();
            } else {
                list = map.get(purchase.getBuyerName());
            }
            list.add(purchase);
            map.put(purchase.getBuyerName(), list);
        }

        //collect TotalJson
        List<Customer> customers = new ArrayList<>();
        stat.setCustomers(customers);

        long diffDates = Math.abs(dateFrom.getTime() - dateTo.getTime());
        long diffDays = TimeUnit.DAYS.convert(diffDates, TimeUnit.MILLISECONDS);
        stat.setTotalDays(diffDays);

        long totalExpensesAll = 0;

        for (Map.Entry<String, List<BuyerRepository.BuyerStatJSON>> entry : map.entrySet()) {
            Customer customer = new Customer();
            customer.setName(entry.getKey());
            customer.setPurchases(entry.getValue());

            long cutomerTotalExpenses = 0;
            for (BuyerRepository.BuyerStatJSON purchase : entry.getValue()) {
                cutomerTotalExpenses += purchase.getExpenses();
                // подсумма для конкретного покупателя (по всем его товарам)
                customer.setTotalExpenses(cutomerTotalExpenses);

                // общая сумма всех товаров всех покупателей
                totalExpensesAll += cutomerTotalExpenses;

                // добавляем в общую коллекцию customers
                customers.add(customer);
            }
            stat.setTotalExpenses(totalExpensesAll);

            double avgExpenses = 0;

            if (totalExpensesAll != 0 && !customers.isEmpty()) {
                avgExpenses = (double) totalExpensesAll / customers.size();
            }
        }

        return stat;
    }
}