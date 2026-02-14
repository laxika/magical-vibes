package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaCost {

    private static final Pattern MANA_SYMBOL = Pattern.compile("\\{([^}]+)}");

    private final int genericCost;
    private final Map<ManaColor, Integer> coloredCosts;
    private final boolean hasX;

    public ManaCost(String manaCostString) {
        int generic = 0;
        boolean foundX = false;
        Map<ManaColor, Integer> colored = new EnumMap<>(ManaColor.class);

        Matcher matcher = MANA_SYMBOL.matcher(manaCostString);
        while (matcher.find()) {
            String symbol = matcher.group(1);
            if (symbol.equals("X")) {
                foundX = true;
            } else {
                try {
                    ManaColor color = ManaColor.fromCode(symbol);
                    colored.merge(color, 1, Integer::sum);
                } catch (IllegalArgumentException e) {
                    generic += Integer.parseInt(symbol);
                }
            }
        }

        this.genericCost = generic;
        this.coloredCosts = colored;
        this.hasX = foundX;
    }

    public boolean hasX() {
        return hasX;
    }

    public int getManaValue() {
        int total = genericCost;
        for (int count : coloredCosts.values()) {
            total += count;
        }
        return total;
    }

    public boolean canPay(ManaPool pool) {
        return canPay(pool, 0);
    }

    public boolean canPay(ManaPool pool, int xValue) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        int remaining = pool.getTotal();
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            remaining -= entry.getValue();
        }

        return remaining >= genericCost + xValue;
    }

    public void pay(ManaPool pool) {
        pay(pool, 0);
    }

    public void pay(ManaPool pool, int xValue) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }

        int remainingGeneric = genericCost + xValue;
        while (remainingGeneric > 0) {
            ManaColor highestColor = null;
            int highestAmount = 0;
            for (ManaColor color : ManaColor.values()) {
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
