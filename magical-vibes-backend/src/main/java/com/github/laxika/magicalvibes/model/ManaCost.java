package com.github.laxika.magicalvibes.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaCost {

    private static final Pattern MANA_SYMBOL = Pattern.compile("\\{([^}]+)}");
    private static final List<String> COLORS = List.of("W", "U", "B", "R", "G");

    private final int genericCost;
    private final Map<String, Integer> coloredCosts;

    public ManaCost(String manaCostString) {
        int generic = 0;
        Map<String, Integer> colored = new HashMap<>();

        Matcher matcher = MANA_SYMBOL.matcher(manaCostString);
        while (matcher.find()) {
            String symbol = matcher.group(1);
            if (COLORS.contains(symbol)) {
                colored.merge(symbol, 1, Integer::sum);
            } else {
                generic += Integer.parseInt(symbol);
            }
        }

        this.genericCost = generic;
        this.coloredCosts = colored;
    }

    public boolean canPay(ManaPool pool) {
        for (Map.Entry<String, Integer> entry : coloredCosts.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        int remaining = pool.getTotal();
        for (Map.Entry<String, Integer> entry : coloredCosts.entrySet()) {
            remaining -= entry.getValue();
        }

        return remaining >= genericCost;
    }

    public void pay(ManaPool pool) {
        for (Map.Entry<String, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }

        int remainingGeneric = genericCost;
        while (remainingGeneric > 0) {
            String highestColor = null;
            int highestAmount = 0;
            for (String color : COLORS) {
                int amount = pool.get(color);
                if (amount > highestAmount) {
                    highestAmount = amount;
                    highestColor = color;
                }
            }
            if (highestColor != null) {
                pool.remove(highestColor);
                remainingGeneric--;
            } else {
                break;
            }
        }
    }
}
