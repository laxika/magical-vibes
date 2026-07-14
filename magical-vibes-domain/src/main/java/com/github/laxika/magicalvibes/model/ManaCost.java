package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaCost {

    private static final Pattern MANA_SYMBOL = Pattern.compile("\\{([^}]+)}");

    /**
     * A hybrid mana symbol. {@code colors} are the colored ways to pay (one mana of any of them);
     * {@code genericAlternative} is the generic amount that may be paid instead for monocolored
     * hybrids like {2/W} (-1 when there is no generic option, e.g. the color-hybrid {W/B}).
     */
    private record HybridSymbol(Set<ManaColor> colors, int genericAlternative) {}

    private final int genericCost;
    private final Map<ManaColor, Integer> coloredCosts;
    private final Map<ManaColor, Integer> phyrexianCosts;
    private final List<HybridSymbol> hybridCosts;
    private final int xSymbolCount;

    public ManaCost(String manaCostString) {
        int generic = 0;
        int xCount = 0;
        Map<ManaColor, Integer> colored = new EnumMap<>(ManaColor.class);
        Map<ManaColor, Integer> phyrexian = new EnumMap<>(ManaColor.class);
        List<HybridSymbol> hybrid = new ArrayList<>();

        Matcher matcher = MANA_SYMBOL.matcher(manaCostString);
        while (matcher.find()) {
            String symbol = matcher.group(1);
            if (symbol.equals("X")) {
                xCount++;
            } else if (symbol.endsWith("/P")) {
                // Phyrexian mana (e.g. R/P) — can be paid with its color or 2 life
                ManaColor color = ManaColor.fromCode(symbol.substring(0, symbol.length() - 2));
                phyrexian.merge(color, 1, Integer::sum);
            } else if (symbol.contains("/")) {
                // Hybrid mana, e.g. {W/B} (pay W or B) or {2/W} (pay 2 generic or W)
                Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
                int genericAlt = -1;
                for (String part : symbol.split("/")) {
                    ManaColor color = ManaColor.fromCode(part);
                    if (color != null) {
                        colors.add(color);
                    } else {
                        genericAlt = Integer.parseInt(part);
                    }
                }
                hybrid.add(new HybridSymbol(colors, genericAlt));
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
        this.hybridCosts = hybrid;
        this.xSymbolCount = xCount;
    }

    public boolean hasX() {
        return xSymbolCount > 0;
    }

    /**
     * Number of mana symbols in this cost that include the given color (chroma counting). A colored
     * symbol like {W}, a Phyrexian symbol {W/P}, and a hybrid symbol containing white ({W/U}, {2/W})
     * each count as one white mana symbol; generic and {X} symbols never count.
     */
    public int countColorSymbols(ManaColor color) {
        int count = coloredCosts.getOrDefault(color, 0);
        count += phyrexianCosts.getOrDefault(color, 0);
        for (HybridSymbol hybrid : hybridCosts) {
            if (hybrid.colors().contains(color)) {
                count++;
            }
        }
        return count;
    }

    /**
     * X-cost-only colorless mana (Rosheen Meanderer) available to pay this cost. It is usable only
     * when the cost contains an {X} symbol, and only for generic portions (it is colorless).
     */
    private int xCostOnlyAvailable(ManaPool pool) {
        return hasX() ? pool.getXCostOnlyColorless() : 0;
    }

    /**
     * Spends x-cost-only colorless mana toward the given generic amount (only if this cost contains
     * {X}), returning the generic still owed afterwards.
     */
    private int spendXCostOnlyForGeneric(ManaPool pool, int remainingGeneric) {
        if (hasX() && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getXCostOnlyColorless());
            pool.removeXCostOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }
        return remainingGeneric;
    }

    /**
     * Number of {X} symbols in the cost. For {X}{X}{X}{W} this is 3, meaning the chosen X value
     * is multiplied by 3 to determine the actual generic mana that must be paid.
     */
    public int getXSymbolCount() {
        return xSymbolCount;
    }

    /**
     * Multiplier applied to the {@code xValue} argument in canPay/pay. Callers for non-X spells
     * pass the additional generic cost modifier (e.g. Thalia +1, Wizard -1) via {@code xValue};
     * that value must be added directly, not zeroed out. For X spells, {@code xValue} is the
     * chosen X value and gets multiplied by the number of {X} symbols (CR 107.3b).
     */
    private int effectiveXMultiplier() {
        return Math.max(1, xSymbolCount);
    }

    /**
     * Returns an unmodifiable view of the colored mana requirements.
     * Used by AI mana management to determine which colors are needed.
     */
    public Map<ManaColor, Integer> getColoredCosts() {
        return Collections.unmodifiableMap(coloredCosts);
    }

    /** The generic (colorless-symbol) portion of the cost, e.g. 5 for "{5}" or "{5}{W}". */
    public int getGenericCost() {
        return genericCost;
    }

    public int getManaValue() {
        int total = genericCost;
        for (int count : coloredCosts.values()) {
            total += count;
        }
        for (int count : phyrexianCosts.values()) {
            total += count;
        }
        for (HybridSymbol hybrid : hybridCosts) {
            // CR 202.3f: a monocolored hybrid {2/W} has mana value 2; a color hybrid {W/B} has 1.
            total += hybrid.genericAlternative() >= 0 ? hybrid.genericAlternative() : 1;
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

    /**
     * Auto-pays Phyrexian symbols, choosing per symbol: colored mana from the pool when the rest
     * of this cost (colored + hybrid + generic + X) stays payable afterwards, otherwise 2 life.
     * {@link #canPay} treats Phyrexian symbols as always satisfiable (paying life is always an
     * option), so auto-payment must never spend mana that the approved payment plan needs
     * elsewhere — a greedy mana-first assignment could otherwise starve the generic part of a
     * cost the pre-check already accepted.
     *
     * @param xValue same semantics as the second argument of {@link #canPay(ManaPool, int)}
     * @return the total life that must be paid
     */
    public int payPhyrexianManaAuto(ManaPool pool, int xValue) {
        Map<ManaColor, Integer> reserved = new EnumMap<>(ManaColor.class);
        int lifeCost = 0;
        for (Map.Entry<ManaColor, Integer> entry : phyrexianCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                reserved.merge(entry.getKey(), 1, Integer::sum);
                if (!canPayRestWithReserved(pool, xValue, reserved)) {
                    reserved.merge(entry.getKey(), -1, Integer::sum);
                    lifeCost += 2;
                }
            }
        }
        for (Map.Entry<ManaColor, Integer> entry : reserved.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }
        return lifeCost;
    }

    /** {@link #canPay(ManaPool, int)} for the non-Phyrexian part, with pool mana pre-reserved for Phyrexian symbols. */
    private boolean canPayRestWithReserved(ManaPool pool, int xValue, Map<ManaColor, Integer> reserved) {
        Map<ManaColor, Integer> available = availableByColor(pool);
        for (Map.Entry<ManaColor, Integer> entry : reserved.entrySet()) {
            int left = available.get(entry.getKey()) - entry.getValue();
            if (left < 0) {
                return false;
            }
            available.put(entry.getKey(), left);
        }
        if (!reserveColoredCosts(available)) {
            return false;
        }
        int[] extraGeneric = {0};
        if (!assignHybrids(available, extraGeneric)) {
            return false;
        }
        int remaining = totalOf(available) - residualFlexibleOvercount(pool) + xCostOnlyAvailable(pool);
        return remaining >= genericCost + extraGeneric[0] + xValue * effectiveXMultiplier();
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
        Map<ManaColor, Integer> available = availableByColor(pool);
        if (!reserveColoredCosts(available)) {
            return false;
        }
        int[] extraGeneric = {0};
        if (!assignHybrids(available, extraGeneric)) {
            return false;
        }
        int remaining = totalOf(available) - residualFlexibleOvercount(pool) + xCostOnlyAvailable(pool);
        return remaining >= genericCost + extraGeneric[0] + xValue * effectiveXMultiplier();
    }

    /**
     * Portion of a pool's {@code flexibleOvercount} not already reflected in its per-color
     * amounts (which {@link ManaPool#get} corrects for). Summing per-color availability
     * double-counts mutually-exclusive taps (e.g. a dual land counted as both R and G), so
     * this must be subtracted from a per-color reconstruction of the generic-payable total.
     * Always 0 for a plain {@link ManaPool}.
     */
    private static int residualFlexibleOvercount(ManaPool pool) {
        int residual = pool.getFlexibleOvercount();
        for (ManaColor color : ManaColor.values()) {
            residual -= pool.getPerColorOvercount(color);
        }
        return Math.max(0, residual);
    }

    // ── Hybrid mana support (shared by the core canPay/pay path) ───────

    private Map<ManaColor, Integer> availableByColor(ManaPool pool) {
        Map<ManaColor, Integer> available = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            available.put(color, pool.get(color));
        }
        return available;
    }

    private boolean reserveColoredCosts(Map<ManaColor, Integer> available) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int left = available.get(entry.getKey()) - entry.getValue();
            if (left < 0) {
                return false;
            }
            available.put(entry.getKey(), left);
        }
        return true;
    }

    private static int totalOf(Map<ManaColor, Integer> available) {
        return available.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Greedily assigns each hybrid symbol (most-constrained first) to one available color, decrementing
     * {@code available}. A monocolored hybrid with no available color falls back to its generic
     * alternative (accumulated into {@code extraGeneric}). Returns false if a color hybrid cannot be
     * satisfied by any of its colors.
     */
    private boolean assignHybrids(Map<ManaColor, Integer> available, int[] extraGeneric) {
        for (HybridSymbol hybrid : hybridsMostConstrainedFirst(available)) {
            ManaColor chosen = pickRichestColor(hybrid.colors(), available);
            if (chosen != null) {
                available.put(chosen, available.get(chosen) - 1);
            } else if (hybrid.genericAlternative() >= 0) {
                extraGeneric[0] += hybrid.genericAlternative();
            } else {
                return false;
            }
        }
        return true;
    }

    private List<HybridSymbol> hybridsMostConstrainedFirst(Map<ManaColor, Integer> available) {
        List<HybridSymbol> sorted = new ArrayList<>(hybridCosts);
        sorted.sort(Comparator.comparingInt(h -> (int) h.colors().stream()
                .filter(c -> available.get(c) > 0).count()));
        return sorted;
    }

    private static ManaColor pickRichestColor(Set<ManaColor> colors, Map<ManaColor, Integer> available) {
        ManaColor best = null;
        int bestAmount = 0;
        for (ManaColor color : colors) {
            int amount = available.get(color);
            if (amount > bestAmount) {
                bestAmount = amount;
                best = color;
            }
        }
        return best;
    }

    /**
     * Assigns hybrid symbols against a per-color availability map (already reduced by fixed colored
     * costs) for the total-based context {@code canPay} overloads. {@code out[0]} accumulates the
     * generic mana owed by monocolored hybrids paid via their generic alternative, {@code out[1]}
     * the number of hybrids paid with colored mana (each reserves one mana from the generic budget).
     * Returns false if a color hybrid cannot be satisfied by any of its colors.
     */
    private boolean assignHybridsCounting(Map<ManaColor, Integer> available, int[] out) {
        for (HybridSymbol hybrid : hybridsMostConstrainedFirst(available)) {
            ManaColor chosen = pickRichestColor(hybrid.colors(), available);
            if (chosen != null) {
                available.put(chosen, available.get(chosen) - 1);
                out[1]++;
            } else if (hybrid.genericAlternative() >= 0) {
                out[0] += hybrid.genericAlternative();
            } else {
                return false;
            }
        }
        return true;
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
            if (instantSorceryOnlyColorlessContext) {
                available += pool.getInstantSorceryOnlyColored(entry.getKey());
            }
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
            for (ManaColor color : ManaColor.values()) {
                if (color == ManaColor.COLORLESS) {
                    continue;
                }
                int coloredNeeded = coloredCosts.getOrDefault(color, 0);
                int regular = pool.get(color);
                int instantSorceryOnlyUsedForColored = Math.max(0, coloredNeeded - regular);
                remaining += pool.getInstantSorceryOnlyColored(color) - instantSorceryOnlyUsedForColored;
            }
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
        remaining += xCostOnlyAvailable(pool);

        int hybridGeneric = 0;
        if (!hybridCosts.isEmpty()) {
            Map<ManaColor, Integer> available = new EnumMap<>(ManaColor.class);
            for (ManaColor color : ManaColor.values()) {
                int amount = pool.get(color);
                if (instantSorceryOnlyColorlessContext) {
                    amount += pool.getInstantSorceryOnlyColored(color);
                }
                if (color == ManaColor.RED) {
                    amount += extraRed;
                } else if (color == ManaColor.GREEN) {
                    amount += extraGreen;
                }
                available.put(color, amount - coloredCosts.getOrDefault(color, 0));
            }
            int[] hybridOut = {0, 0};
            if (!assignHybridsCounting(available, hybridOut)) {
                return false;
            }
            hybridGeneric = hybridOut[0] + hybridOut[1];
        }

        return remaining >= genericCost + hybridGeneric + xValue * effectiveXMultiplier();
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, null);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext) {
        boolean hasCreatureCtx = subtypeCreatureContext != null && !subtypeCreatureContext.isEmpty();
        boolean hasSpellOrAbilityCtx = subtypeSpellOrAbilityContext != null && !subtypeSpellOrAbilityContext.isEmpty();
        if (!hasCreatureCtx && !hasSpellOrAbilityCtx) {
            return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
        }
        Set<CardSubtype> creatureCtx = hasCreatureCtx ? subtypeCreatureContext : Set.of();
        Set<CardSubtype> soaCtx = hasSpellOrAbilityCtx ? subtypeSpellOrAbilityContext : Set.of();
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        // Check each colored cost can be paid from combined sources
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int available = pool.get(entry.getKey());
            available += pool.getSubtypeCreatureManaForColor(creatureCtx, entry.getKey());
            available += pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, entry.getKey());
            if (instantSorceryOnlyColorlessContext) {
                available += pool.getInstantSorceryOnlyColored(entry.getKey());
            }
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

        // Generic feasibility as a pure total check across every usable bucket. Colored feasibility is
        // already verified per-color above, and all these buckets are fully flexible for this spell
        // (colored buckets pay their color or generic; colorless-only buckets pay generic), so a total
        // check avoids the fragile per-restriction compensation that double-counts when a colored cost
        // is covered by more than one flexible bucket (e.g. a {R} cost paid from subtype mana while
        // restrictedRedContext is also set for a creature spell).
        int totalColored = 0;
        for (int need : coloredCosts.values()) {
            totalColored += need;
        }
        int totalUsable = pool.getTotal();
        if (artifactContext) {
            totalUsable += pool.getArtifactOnlyColorless();
        }
        if (myrContext) {
            totalUsable += pool.getMyrOnlyColorless();
        }
        if (instantSorceryOnlyColorlessContext) {
            totalUsable += pool.getInstantSorceryOnlyColorless() + pool.getInstantSorceryOnlyColoredTotal();
        }
        if (restrictedRedContext) {
            totalUsable += extraRed;
        }
        if (kickedOnlyGreenContext) {
            totalUsable += extraGreen;
        }
        totalUsable += pool.getSubtypeCreatureManaTotal(creatureCtx);
        totalUsable += pool.getSubtypeSpellOrAbilityManaTotal(soaCtx);
        totalUsable += xCostOnlyAvailable(pool);

        int hybridGeneric = 0;
        if (!hybridCosts.isEmpty()) {
            Map<ManaColor, Integer> available = new EnumMap<>(ManaColor.class);
            for (ManaColor color : ManaColor.values()) {
                int amount = pool.get(color);
                amount += pool.getSubtypeCreatureManaForColor(creatureCtx, color);
                amount += pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, color);
                if (instantSorceryOnlyColorlessContext) {
                    amount += pool.getInstantSorceryOnlyColored(color);
                }
                if (color == ManaColor.RED) {
                    amount += extraRed;
                } else if (color == ManaColor.GREEN) {
                    amount += extraGreen;
                }
                available.put(color, amount - coloredCosts.getOrDefault(color, 0));
            }
            int[] hybridOut = {0, 0};
            if (!assignHybridsCounting(available, hybridOut)) {
                return false;
            }
            hybridGeneric = hybridOut[0] + hybridOut[1];
        }

        return totalUsable - totalColored >= genericCost + hybridGeneric + xValue * effectiveXMultiplier();
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

        return remaining >= genericCost + xValue * effectiveXMultiplier();
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

        int remainingGeneric = genericCost + xValue * effectiveXMultiplier();

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

        int remaining = pool.getTotal() + pool.getXCostOnlyColorless();
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

        // Pay hybrid symbols: assign each to an available color (or its generic alternative), then
        // remove the colors the assignment consumed from the pool.
        int extraHybridGeneric = payHybrids(pool);

        int remainingGeneric = genericCost + extraHybridGeneric + xValue * effectiveXMultiplier();
        remainingGeneric = spendXCostOnlyForGeneric(pool, remainingGeneric);
        payGenericPreferColorless(pool, remainingGeneric);
    }

    /**
     * Spends colored mana for the hybrid symbols (after fixed colored costs are already paid) and
     * returns the additional generic mana owed for any monocolored hybrids paid via their generic
     * alternative. Assumes the cost is affordable (callers gate on {@link #canPay}).
     */
    private int payHybrids(ManaPool pool) {
        if (hybridCosts.isEmpty()) {
            return 0;
        }
        Map<ManaColor, Integer> available = availableByColor(pool);
        Map<ManaColor, Integer> before = new EnumMap<>(available);
        int[] extraGeneric = {0};
        assignHybrids(available, extraGeneric);
        for (ManaColor color : ManaColor.values()) {
            int spent = before.get(color) - available.get(color);
            for (int i = 0; i < spent; i++) {
                pool.remove(color);
            }
        }
        return extraGeneric[0];
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
                } else if (instantSorceryOnlyColorlessContext && pool.getInstantSorceryOnlyColored(entry.getKey()) > 0) {
                    pool.removeInstantSorceryOnlyColored(entry.getKey(), 1);
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        int remainingGeneric = genericCost + xValue * effectiveXMultiplier();

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

        // Spend instant/sorcery-only colored mana for generic costs
        if (instantSorceryOnlyColorlessContext && remainingGeneric > 0) {
            for (ManaColor color : ManaColor.values()) {
                if (remainingGeneric <= 0) {
                    break;
                }
                if (color == ManaColor.COLORLESS) {
                    continue;
                }
                int fromRestricted = Math.min(remainingGeneric, pool.getInstantSorceryOnlyColored(color));
                pool.removeInstantSorceryOnlyColored(color, fromRestricted);
                remainingGeneric -= fromRestricted;
            }
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

        remainingGeneric = spendXCostOnlyForGeneric(pool, remainingGeneric);

        payGenericPreferColorless(pool, remainingGeneric);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, null);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext) {
        boolean hasCreatureCtx = subtypeCreatureContext != null && !subtypeCreatureContext.isEmpty();
        boolean hasSpellOrAbilityCtx = subtypeSpellOrAbilityContext != null && !subtypeSpellOrAbilityContext.isEmpty();
        if (!hasCreatureCtx && !hasSpellOrAbilityCtx) {
            pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
            return;
        }
        Set<CardSubtype> creatureCtx = hasCreatureCtx ? subtypeCreatureContext : Set.of();
        Set<CardSubtype> soaCtx = hasSpellOrAbilityCtx ? subtypeSpellOrAbilityContext : Set.of();
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                // Prefer spending subtype mana first (most restricted)
                if (pool.getSubtypeCreatureManaForColor(creatureCtx, entry.getKey()) > 0) {
                    pool.removeSubtypeCreatureMana(creatureCtx, entry.getKey(), 1);
                } else if (pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, entry.getKey()) > 0) {
                    pool.removeSubtypeSpellOrAbilityMana(soaCtx, entry.getKey(), 1);
                } else if (restrictedRedContext && entry.getKey() == ManaColor.RED && extraRed > 0) {
                    pool.removeRestrictedRed(1);
                    extraRed--;
                } else if (kickedOnlyGreenContext && entry.getKey() == ManaColor.GREEN && extraGreen > 0) {
                    pool.removeKickedOnlyGreen(1);
                    extraGreen--;
                } else if (instantSorceryOnlyColorlessContext && pool.getInstantSorceryOnlyColored(entry.getKey()) > 0) {
                    pool.removeInstantSorceryOnlyColored(entry.getKey(), 1);
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        int remainingGeneric = genericCost + xValue * effectiveXMultiplier();

        // Spend subtype mana for generic costs first (most restricted)
        if (remainingGeneric > 0) {
            int subtypeTotal = pool.getSubtypeCreatureManaTotal(creatureCtx);
            int fromSubtype = Math.min(remainingGeneric, subtypeTotal);
            if (fromSubtype > 0) {
                // Remove from subtype pools color by color
                int toRemove = fromSubtype;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getSubtypeCreatureManaForColor(creatureCtx, color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeSubtypeCreatureMana(creatureCtx, color, removeNow);
                        toRemove -= removeNow;
                    }
                }
                remainingGeneric -= fromSubtype;
            }
        }

        // Spend subtype spell-or-ability mana for generic costs (also fully restricted)
        if (remainingGeneric > 0) {
            int subtypeTotal = pool.getSubtypeSpellOrAbilityManaTotal(soaCtx);
            int fromSubtype = Math.min(remainingGeneric, subtypeTotal);
            if (fromSubtype > 0) {
                int toRemove = fromSubtype;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeSubtypeSpellOrAbilityMana(soaCtx, color, removeNow);
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

        if (instantSorceryOnlyColorlessContext && remainingGeneric > 0) {
            for (ManaColor color : ManaColor.values()) {
                if (remainingGeneric <= 0) {
                    break;
                }
                if (color == ManaColor.COLORLESS) {
                    continue;
                }
                int fromRestricted = Math.min(remainingGeneric, pool.getInstantSorceryOnlyColored(color));
                pool.removeInstantSorceryOnlyColored(color, fromRestricted);
                remainingGeneric -= fromRestricted;
            }
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

        remainingGeneric = spendXCostOnlyForGeneric(pool, remainingGeneric);

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
