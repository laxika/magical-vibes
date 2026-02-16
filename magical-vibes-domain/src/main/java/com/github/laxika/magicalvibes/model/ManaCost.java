package com.github.laxika.magicalvibes.model;

import java.util.EnumMap;
import java.util.List;
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

    public boolean canPay(ManaPool pool, int xValue, ManaColor xColorRestriction, int additionalGenericCost) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        int restrictedAvailable = pool.get(xColorRestriction);
        if (coloredCosts.containsKey(xColorRestriction)) {
            restrictedAvailable -= coloredCosts.get(xColorRestriction);
        }
        if (restrictedAvailable < xValue) {
            return false;
        }

        int remaining = pool.getTotal();
        for (int count : coloredCosts.values()) {
            remaining -= count;
        }
        remaining -= xValue;

        return remaining >= genericCost + additionalGenericCost;
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

    public void pay(ManaPool pool, int xValue, ManaColor xColorRestriction, int additionalGenericCost) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }

        for (int i = 0; i < xValue; i++) {
            pool.remove(xColorRestriction);
        }

        int remainingGeneric = genericCost + additionalGenericCost;
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

    /**
     * Check if the cost can be paid with convoke contributions.
     * Each convoke contribution pays for one mana: colored if it matches an unpaid colored cost,
     * otherwise reduces generic cost.
     */
    /**
     * Check if the cost can be paid with convoke contributions.
     * Each convoke contribution pays for one mana: colored if it matches an unpaid colored cost,
     * otherwise reduces generic cost. Null entries represent colorless creatures (generic only).
     */
    public boolean canPayWithConvoke(ManaPool pool, int additionalGenericCost, List<ManaColor> convokeContributions) {
        // Calculate remaining costs after convoke
        Map<ManaColor, Integer> remainingColored = new EnumMap<>(ManaColor.class);
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            remainingColored.put(entry.getKey(), entry.getValue());
        }
        int remainingGeneric = genericCost + additionalGenericCost;

        for (ManaColor contribution : convokeContributions) {
            if (contribution != null) {
                int coloredRemaining = remainingColored.getOrDefault(contribution, 0);
                if (coloredRemaining > 0) {
                    remainingColored.put(contribution, coloredRemaining - 1);
                } else if (remainingGeneric > 0) {
                    remainingGeneric--;
                }
            } else {
                // Colorless creature can only pay generic
                if (remainingGeneric > 0) {
                    remainingGeneric--;
                }
            }
        }

        // Check pool can pay the remaining
        for (Map.Entry<ManaColor, Integer> entry : remainingColored.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        int poolRemaining = pool.getTotal();
        for (Map.Entry<ManaColor, Integer> entry : remainingColored.entrySet()) {
            poolRemaining -= entry.getValue();
        }

        return poolRemaining >= remainingGeneric;
    }

    /**
     * Pay the cost using convoke contributions and the mana pool.
     * Each convoke contribution pays for one mana: colored if it matches an unpaid colored cost,
     * otherwise reduces generic cost.
     */
    public void payWithConvoke(ManaPool pool, int additionalGenericCost, List<ManaColor> convokeContributions) {
        // Calculate remaining costs after convoke
        Map<ManaColor, Integer> remainingColored = new EnumMap<>(ManaColor.class);
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            remainingColored.put(entry.getKey(), entry.getValue());
        }
        int remainingGeneric = genericCost + additionalGenericCost;

        for (ManaColor contribution : convokeContributions) {
            if (contribution != null) {
                int coloredRemaining = remainingColored.getOrDefault(contribution, 0);
                if (coloredRemaining > 0) {
                    remainingColored.put(contribution, coloredRemaining - 1);
                } else if (remainingGeneric > 0) {
                    remainingGeneric--;
                }
            } else {
                // Colorless creature can only pay generic
                if (remainingGeneric > 0) {
                    remainingGeneric--;
                }
            }
        }

        // Pay remaining colored costs from pool
        for (Map.Entry<ManaColor, Integer> entry : remainingColored.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }

        // Pay remaining generic from pool
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
