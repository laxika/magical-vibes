package com.github.laxika.magicalvibes.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaCost {

    private static final Pattern MANA_SYMBOL = Pattern.compile("\\{([^}]+)}");

    private final int genericCost;
    private final Map<ManaColor, Integer> coloredCosts;
    private final Map<ManaColor, Integer> phyrexianCosts;
    private final int xSymbolCount;

    public ManaCost(String manaCostString) {
        int generic = 0;
        int xCount = 0;
        Map<ManaColor, Integer> colored = new EnumMap<>(ManaColor.class);
        Map<ManaColor, Integer> phyrexian = new EnumMap<>(ManaColor.class);

        Matcher matcher = MANA_SYMBOL.matcher(manaCostString);
        while (matcher.find()) {
            String symbol = matcher.group(1);
            if (symbol.equals("X")) {
                xCount++;
            } else if (symbol.endsWith("/P")) {
                // Phyrexian mana (e.g. R/P) — can be paid with its color or 2 life
                ManaColor color = ManaColor.fromCode(symbol.substring(0, symbol.length() - 2));
                phyrexian.merge(color, 1, Integer::sum);
            } else {
                ManaColor color = ManaColor.fromCode(symbol);
                if (color != null) {
                    colored.merge(color, 1, Integer::sum);
                } else {
                    generic += Integer.parseInt(symbol);
                }
            }
        }

        this.genericCost = generic;
        this.coloredCosts = colored;
        this.phyrexianCosts = phyrexian;
        this.xSymbolCount = xCount;
    }

    public boolean hasX() {
        return xSymbolCount > 0;
    }

    /**
     * Number of {X} symbols in the cost. For {X}{X}{X}{W} this is 3, meaning the chosen X value
     * is multiplied by 3 to determine the actual generic mana that must be paid.
     */
    public int getXSymbolCount() {
        return xSymbolCount;
    }

    /**
     * Returns an unmodifiable view of the colored mana requirements.
     * Used by AI mana management to determine which colors are needed.
     */
    public Map<ManaColor, Integer> getColoredCosts() {
        return Collections.unmodifiableMap(coloredCosts);
    }

    public int getManaValue() {
        int total = genericCost;
        for (int count : coloredCosts.values()) {
            total += count;
        }
        for (int count : phyrexianCosts.values()) {
            total += count;
        }
        return total;
    }

    public boolean hasPhyrexianMana() {
        return !phyrexianCosts.isEmpty();
    }

    /**
     * Pays Phyrexian mana costs. For each Phyrexian symbol, uses colored mana from the pool
     * if available; otherwise the cost must be paid with 2 life per symbol.
     *
     * @return the total life that must be paid for Phyrexian symbols not covered by mana
     */
    public int payPhyrexianMana(ManaPool pool) {
        return payPhyrexianMana(pool, null);
    }

    /**
     * Pays Phyrexian mana costs with player choice.
     * If requestedLifeCount is null, auto-pays (prefers mana, falls back to life).
     * If requestedLifeCount is specified, pays exactly that many symbols with life (2 each)
     * and the rest with colored mana.
     *
     * @return the total life that must be paid
     */
    public int payPhyrexianMana(ManaPool pool, Integer requestedLifeCount) {
        int totalPhyrexian = phyrexianCosts.values().stream().mapToInt(Integer::intValue).sum();
        int lifeSymbols = requestedLifeCount != null
                ? Math.max(0, Math.min(requestedLifeCount, totalPhyrexian))
                : 0;
        boolean autoMode = requestedLifeCount == null;

        int lifeCost = 0;
        int lifeSymbolsRemaining = lifeSymbols;
        for (Map.Entry<ManaColor, Integer> entry : phyrexianCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                if (autoMode) {
                    // Auto: prefer mana, fall back to life
                    if (pool.get(entry.getKey()) > 0) {
                        pool.remove(entry.getKey());
                    } else {
                        lifeCost += 2;
                    }
                } else {
                    // Player chose: pay life symbols first, then mana
                    if (lifeSymbolsRemaining > 0) {
                        lifeCost += 2;
                        lifeSymbolsRemaining--;
                    } else {
                        pool.remove(entry.getKey());
                    }
                }
            }
        }
        return lifeCost;
    }

    public int getPhyrexianManaCount() {
        return phyrexianCosts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean canPayCreatureOnly(ManaPool pool) {
        return canPayCreatureOnly(pool, 0);
    }

    public boolean canPayCreatureOnly(ManaPool pool, int additionalGenericCost) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (pool.getCreatureMana(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        int remaining = pool.getCreatureManaTotal();
        for (int count : coloredCosts.values()) {
            remaining -= count;
        }

        return remaining >= genericCost + additionalGenericCost;
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

        return remaining >= genericCost + xValue * xSymbolCount;
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext) {
        return canPay(pool, xValue, artifactContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext) {
        return canPay(pool, xValue, artifactContext, myrContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext) {
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int available = pool.get(entry.getKey());
            if (entry.getKey() == ManaColor.RED) {
                available += extraRed;
            }
            if (entry.getKey() == ManaColor.GREEN) {
                available += extraGreen;
            }
            if (available < entry.getValue()) {
                return false;
            }
        }

        int remaining = pool.getTotal();
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            remaining -= entry.getValue();
        }

        if (artifactContext) {
            remaining += pool.getArtifactOnlyColorless();
        }
        if (myrContext) {
            remaining += pool.getMyrOnlyColorless();
        }
        if (instantSorceryOnlyColorlessContext) {
            remaining += pool.getInstantSorceryOnlyColorless();
        }
        if (restrictedRedContext) {
            int redNeeded = coloredCosts.getOrDefault(ManaColor.RED, 0);
            int regularRed = pool.get(ManaColor.RED);
            int restrictedRedUsedForColored = Math.max(0, redNeeded - regularRed);
            remaining += extraRed - restrictedRedUsedForColored;
        }
        if (kickedOnlyGreenContext) {
            int greenNeeded = coloredCosts.getOrDefault(ManaColor.GREEN, 0);
            int regularGreen = pool.get(ManaColor.GREEN);
            int kickedOnlyGreenUsedForColored = Math.max(0, greenNeeded - regularGreen);
            remaining += extraGreen - kickedOnlyGreenUsedForColored;
        }

        return remaining >= genericCost + xValue * xSymbolCount;
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext) {
        if (subtypeCreatureContext == null || subtypeCreatureContext.isEmpty()) {
            return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
        }
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        // Check each colored cost can be paid from combined sources
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int available = pool.get(entry.getKey());
            available += pool.getSubtypeCreatureManaForColor(subtypeCreatureContext, entry.getKey());
            if (entry.getKey() == ManaColor.RED) {
                available += extraRed;
            }
            if (entry.getKey() == ManaColor.GREEN) {
                available += extraGreen;
            }
            if (available < entry.getValue()) {
                return false;
            }
        }

        // Check generic costs: total available (regular + all restricted) minus colored costs
        int remaining = pool.getTotal();
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            remaining -= entry.getValue();
        }

        if (artifactContext) {
            remaining += pool.getArtifactOnlyColorless();
        }
        if (myrContext) {
            remaining += pool.getMyrOnlyColorless();
        }
        if (instantSorceryOnlyColorlessContext) {
            remaining += pool.getInstantSorceryOnlyColorless();
        }
        if (restrictedRedContext) {
            int redNeeded = coloredCosts.getOrDefault(ManaColor.RED, 0);
            int regularRed = pool.get(ManaColor.RED);
            int restrictedRedUsedForColored = Math.max(0, redNeeded - regularRed);
            remaining += extraRed - restrictedRedUsedForColored;
        }
        if (kickedOnlyGreenContext) {
            int greenNeeded = coloredCosts.getOrDefault(ManaColor.GREEN, 0);
            int regularGreen = pool.get(ManaColor.GREEN);
            int kickedOnlyGreenUsedForColored = Math.max(0, greenNeeded - regularGreen);
            remaining += extraGreen - kickedOnlyGreenUsedForColored;
        }
        // Subtype creature mana: add the full total. The colored check above ensures each color
        // has enough individually, and the total (regular + subtype) minus colored costs correctly
        // accounts for subtype mana used for colored costs being compensated.
        remaining += pool.getSubtypeCreatureManaTotal(subtypeCreatureContext);

        return remaining >= genericCost + xValue * xSymbolCount;
    }

    /**
     * Checks whether the pool (regular + flashback-only mana) has enough to pay.
     * Flashback-only mana can pay both colored and generic costs of flashback spells.
     */
    public boolean canPayFlashback(ManaPool pool, int xValue) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int available = pool.get(entry.getKey()) + pool.getFlashbackOnlyMana(entry.getKey());
            if (available < entry.getValue()) {
                return false;
            }
        }

        int remaining = pool.getTotal() + pool.getFlashbackOnlyManaTotal();
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            remaining -= entry.getValue();
        }

        return remaining >= genericCost + xValue * xSymbolCount;
    }

    /**
     * Pays the mana cost using flashback-only mana first, then regular mana.
     */
    public void payFlashback(ManaPool pool, int xValue) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                // Prefer spending flashback-only mana first (more restricted = use first)
                if (pool.getFlashbackOnlyMana(entry.getKey()) > 0) {
                    pool.removeFlashbackOnlyMana(entry.getKey(), 1);
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        int remainingGeneric = genericCost + xValue * xSymbolCount;

        // Spend flashback-only mana for generic costs first (most restricted)
        if (remainingGeneric > 0) {
            int flashbackTotal = pool.getFlashbackOnlyManaTotal();
            int fromFlashback = Math.min(remainingGeneric, flashbackTotal);
            if (fromFlashback > 0) {
                int toRemove = fromFlashback;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getFlashbackOnlyMana(color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeFlashbackOnlyMana(color, removeNow);
                        toRemove -= removeNow;
                    }
                }
                remainingGeneric -= fromFlashback;
            }
        }

        payGenericPreferColorless(pool, remainingGeneric);
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
        if (restrictedAvailable < xValue * xSymbolCount) {
            return false;
        }

        int remaining = pool.getTotal();
        for (int count : coloredCosts.values()) {
            remaining -= count;
        }
        remaining -= xValue * xSymbolCount;

        return remaining >= genericCost + additionalGenericCost;
    }

    /**
     * Calculates the maximum X value that can be paid with the given mana pool (unrestricted X).
     * Returns 0 if the base cost (colored + generic) cannot be paid.
     */
    public int calculateMaxX(ManaPool pool) {
        if (xSymbolCount <= 0) {
            return 0;
        }
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return 0;
            }
        }

        int remaining = pool.getTotal();
        for (int count : coloredCosts.values()) {
            remaining -= count;
        }

        return Math.max(0, (remaining - genericCost) / xSymbolCount);
    }

    /**
     * Calculates the maximum X value that can be paid with the given mana pool
     * when X must be paid with a specific color (e.g., Consume Spirit requires {B} for X).
     * Returns 0 if the base cost cannot be paid.
     */
    public int calculateMaxX(ManaPool pool, ManaColor xColorRestriction, int additionalGenericCost) {
        if (xSymbolCount <= 0) {
            return 0;
        }
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return 0;
            }
        }

        int restrictedAvailable = pool.get(xColorRestriction);
        if (coloredCosts.containsKey(xColorRestriction)) {
            restrictedAvailable -= coloredCosts.get(xColorRestriction);
        }

        int remaining = pool.getTotal();
        for (int count : coloredCosts.values()) {
            remaining -= count;
        }

        int maxFromGeneric = remaining - genericCost - additionalGenericCost;
        int cap = Math.min(restrictedAvailable, maxFromGeneric);
        return Math.max(0, cap / xSymbolCount);
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

        payGenericPreferColorless(pool, genericCost + xValue * xSymbolCount);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext) {
        pay(pool, xValue, artifactContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext) {
        pay(pool, xValue, artifactContext, myrContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext) {
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                if (restrictedRedContext && entry.getKey() == ManaColor.RED && extraRed > 0) {
                    // Prefer spending restricted mana first (more restricted = use first)
                    pool.removeRestrictedRed(1);
                    extraRed--;
                } else if (kickedOnlyGreenContext && entry.getKey() == ManaColor.GREEN && extraGreen > 0) {
                    // Prefer spending kicked-only green first (more restricted = use first)
                    pool.removeKickedOnlyGreen(1);
                    extraGreen--;
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        int remainingGeneric = genericCost + xValue * xSymbolCount;

        // Spend more-restrictive mana first: Myr-only before artifact-only
        if (myrContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getMyrOnlyColorless());
            pool.removeMyrOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        // Spend artifact-only colorless for generic costs
        if (artifactContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getArtifactOnlyColorless());
            pool.removeArtifactOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        // Spend instant/sorcery-only colorless for generic costs
        if (instantSorceryOnlyColorlessContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getInstantSorceryOnlyColorless());
            pool.removeInstantSorceryOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        // Spend creature-or-artifact-only red for generic costs
        if (restrictedRedContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, extraRed);
            pool.removeRestrictedRed(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        // Spend kicked-only green for generic costs
        if (kickedOnlyGreenContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, extraGreen);
            pool.removeKickedOnlyGreen(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        payGenericPreferColorless(pool, remainingGeneric);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext) {
        if (subtypeCreatureContext == null || subtypeCreatureContext.isEmpty()) {
            pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
            return;
        }
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                // Prefer spending subtype creature mana first (most restricted)
                int subtypeAvail = pool.getSubtypeCreatureManaForColor(subtypeCreatureContext, entry.getKey());
                if (subtypeAvail > 0) {
                    pool.removeSubtypeCreatureMana(subtypeCreatureContext, entry.getKey(), 1);
                } else if (restrictedRedContext && entry.getKey() == ManaColor.RED && extraRed > 0) {
                    pool.removeRestrictedRed(1);
                    extraRed--;
                } else if (kickedOnlyGreenContext && entry.getKey() == ManaColor.GREEN && extraGreen > 0) {
                    pool.removeKickedOnlyGreen(1);
                    extraGreen--;
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        int remainingGeneric = genericCost + xValue * xSymbolCount;

        // Spend subtype creature mana for generic costs first (most restricted)
        if (remainingGeneric > 0) {
            int subtypeTotal = pool.getSubtypeCreatureManaTotal(subtypeCreatureContext);
            int fromSubtype = Math.min(remainingGeneric, subtypeTotal);
            if (fromSubtype > 0) {
                // Remove from subtype pools color by color
                int toRemove = fromSubtype;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getSubtypeCreatureManaForColor(subtypeCreatureContext, color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeSubtypeCreatureMana(subtypeCreatureContext, color, removeNow);
                        toRemove -= removeNow;
                    }
                }
                remainingGeneric -= fromSubtype;
            }
        }

        // Spend more-restrictive mana first: Myr-only before artifact-only
        if (myrContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getMyrOnlyColorless());
            pool.removeMyrOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (artifactContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getArtifactOnlyColorless());
            pool.removeArtifactOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (instantSorceryOnlyColorlessContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getInstantSorceryOnlyColorless());
            pool.removeInstantSorceryOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (restrictedRedContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, extraRed);
            pool.removeRestrictedRed(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (kickedOnlyGreenContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, extraGreen);
            pool.removeKickedOnlyGreen(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        payGenericPreferColorless(pool, remainingGeneric);
    }

    public void pay(ManaPool pool, int xValue, ManaColor xColorRestriction, int additionalGenericCost) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }

        int totalX = xValue * xSymbolCount;
        for (int i = 0; i < totalX; i++) {
            pool.remove(xColorRestriction);
        }

        payGenericPreferColorless(pool, genericCost + additionalGenericCost);
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
        payGenericPreferColorless(pool, remainingGeneric);
    }

    /**
     * Checks whether the pool has enough total mana to pay the mana value,
     * ignoring color requirements (mana of any type can be spent).
     */
    public boolean canPayAsGeneric(ManaPool pool) {
        return pool.getTotal() >= getManaValue();
    }

    /**
     * Pays the full mana value using any mana from the pool, ignoring color requirements
     * (mana of any type can be spent to cast the spell).
     */
    public void payAsGeneric(ManaPool pool) {
        payGenericPreferColorless(pool, getManaValue());
    }

    private void payGenericPreferColorless(ManaPool pool, int remainingGeneric) {
        // Prefer colorless mana for generic costs since it can only pay generic,
        // while colored mana is more versatile (can pay both colored and generic).
        while (remainingGeneric > 0 && pool.get(ManaColor.COLORLESS) > 0) {
            pool.remove(ManaColor.COLORLESS);
            remainingGeneric--;
        }
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
