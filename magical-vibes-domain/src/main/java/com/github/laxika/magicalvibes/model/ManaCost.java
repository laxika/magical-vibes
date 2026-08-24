package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManaCost {

    private static final Pattern MANA_SYMBOL = Pattern.compile("\\{([^}]+)}");

    /**
     * A hybrid mana symbol. {@code colors} are the colored ways to pay (one mana of any of them);
     * {@code genericAlternative} is the generic amount that may be paid instead for monocolored
     * hybrids like {2/W} (-1 when there is no generic option, e.g. the color-hybrid {W/B}).
     */
    private record HybridSymbol(Set<ManaColor> colors, int genericAlternative,
                                boolean phyrexianAlternative) {}

    private record ConvokePaymentState(int contributionIndex, int remainingGeneric,
                                       Map<ManaColor, Integer> remainingColored,
                                       List<HybridSymbol> remainingHybrids) {
        private ConvokePaymentState {
            remainingColored = Map.copyOf(remainingColored);
            remainingHybrids = List.copyOf(remainingHybrids);
        }
    }

    private final int genericCost;
    private final Map<ManaColor, Integer> coloredCosts;
    private final Map<ManaColor, Integer> phyrexianCosts;
    private final List<HybridSymbol> hybridCosts;
    private final int snowCost;
    private final int xSymbolCount;
    /** When true, canPay/pay may spend cumulative-upkeep-only mana buckets. */
    private final boolean cumulativeUpkeepPayment;

    public ManaCost(String manaCostString) {
        this(manaCostString, false);
    }

    public ManaCost(String manaCostString, boolean cumulativeUpkeepPayment) {
        int generic = 0;
        int snow = 0;
        int xCount = 0;
        Map<ManaColor, Integer> colored = new EnumMap<>(ManaColor.class);
        Map<ManaColor, Integer> phyrexian = new EnumMap<>(ManaColor.class);
        List<HybridSymbol> hybrid = new ArrayList<>();

        Matcher matcher = MANA_SYMBOL.matcher(manaCostString);
        while (matcher.find()) {
            String symbol = matcher.group(1);
            if (symbol.equals("X")) {
                xCount++;
            } else if (symbol.equals("S")) {
                snow++;
            } else if (symbol.endsWith("/P")) {
                // Phyrexian mana (e.g. R/P) — can be paid with its color or 2 life. Hybrid
                // Phyrexian symbols such as R/G/P can use either listed color or 2 life.
                String phyrexianPart = symbol.substring(0, symbol.length() - 2);
                if (phyrexianPart.contains("/")) {
                    Set<ManaColor> colors = EnumSet.noneOf(ManaColor.class);
                    int genericAlternative = -1;
                    for (String part : phyrexianPart.split("/")) {
                        ManaColor color = ManaColor.fromCode(part);
                        if (color != null) {
                            colors.add(color);
                        } else {
                            genericAlternative = Integer.parseInt(part);
                        }
                    }
                    hybrid.add(new HybridSymbol(colors, genericAlternative, true));
                } else {
                    ManaColor color = ManaColor.fromCode(phyrexianPart);
                    phyrexian.merge(color, 1, Integer::sum);
                }
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
                hybrid.add(new HybridSymbol(colors, genericAlt, false));
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
        this.snowCost = snow;
        this.xSymbolCount = xCount;
        this.cumulativeUpkeepPayment = cumulativeUpkeepPayment;
    }

    private ManaCost(int genericCost, Map<ManaColor, Integer> coloredCosts,
                     Map<ManaColor, Integer> phyrexianCosts, List<HybridSymbol> hybridCosts,
                     int snowCost, int xSymbolCount, boolean cumulativeUpkeepPayment) {
        this.genericCost = genericCost;
        this.coloredCosts = new EnumMap<>(coloredCosts);
        this.phyrexianCosts = new EnumMap<>(phyrexianCosts);
        this.hybridCosts = List.copyOf(hybridCosts);
        this.snowCost = snowCost;
        this.xSymbolCount = xSymbolCount;
        this.cumulativeUpkeepPayment = cumulativeUpkeepPayment;
    }

    private ManaCost(ManaCost source, int snowCost) {
        this(source.genericCost, source.coloredCosts, source.phyrexianCosts, source.hybridCosts,
                snowCost, source.xSymbolCount, source.cumulativeUpkeepPayment);
    }

    /** Returns this cost with every component of {@code increase} added to it. */
    public ManaCost increasedBy(ManaCost increase) {
        if (increase == null) {
            return this;
        }
        Map<ManaColor, Integer> combinedColored = new EnumMap<>(coloredCosts);
        increase.coloredCosts.forEach((color, count) -> combinedColored.merge(color, count, Integer::sum));
        Map<ManaColor, Integer> combinedPhyrexian = new EnumMap<>(phyrexianCosts);
        increase.phyrexianCosts.forEach((color, count) -> combinedPhyrexian.merge(color, count, Integer::sum));
        List<HybridSymbol> combinedHybrid = new ArrayList<>(hybridCosts);
        combinedHybrid.addAll(increase.hybridCosts);
        return new ManaCost(
                genericCost + increase.genericCost,
                combinedColored,
                combinedPhyrexian,
                combinedHybrid,
                snowCost + increase.snowCost,
                xSymbolCount + increase.xSymbolCount,
                cumulativeUpkeepPayment);
    }

    /**
     * Returns this cost after reducing it by the regular generic and colored components of
     * {@code reduction}. Matching colored components reduce the same colored requirements first;
     * unmatched colored components reduce generic mana, as does the generic component.
     */
    public ManaCost reducedBy(ManaCost reduction) {
        if (reduction == null) {
            return this;
        }
        Map<ManaColor, Integer> remainingColored = new EnumMap<>(coloredCosts);
        int genericReduction = reduction.genericCost;
        for (Map.Entry<ManaColor, Integer> entry : reduction.coloredCosts.entrySet()) {
            int matching = Math.min(remainingColored.getOrDefault(entry.getKey(), 0), entry.getValue());
            if (matching > 0) {
                remainingColored.merge(entry.getKey(), -matching, Integer::sum);
            }
            genericReduction += entry.getValue() - matching;
        }
        for (Map.Entry<ManaColor, Integer> entry : reduction.phyrexianCosts.entrySet()) {
            int matching = Math.min(remainingColored.getOrDefault(entry.getKey(), 0), entry.getValue());
            if (matching > 0) {
                remainingColored.merge(entry.getKey(), -matching, Integer::sum);
            }
            genericReduction += entry.getValue() - matching;
        }
        return new ManaCost(
                Math.max(0, genericCost - genericReduction),
                remainingColored, phyrexianCosts, hybridCosts, snowCost, xSymbolCount,
                cumulativeUpkeepPayment);
    }

    /**
     * Returns this cost after reducing only matching colored components of {@code reduction}.
     * Unmatched colored reduction is discarded instead of reducing generic mana.
     */
    public ManaCost reducedByColoredOnly(ManaCost reduction) {
        if (reduction == null) {
            return this;
        }
        Map<ManaColor, Integer> remainingColored = new EnumMap<>(coloredCosts);
        Map<ManaColor, Integer> remainingPhyrexian = new EnumMap<>(phyrexianCosts);
        List<HybridSymbol> remainingHybrid = new ArrayList<>(hybridCosts);
        for (Map.Entry<ManaColor, Integer> entry : reduction.coloredCosts.entrySet()) {
            int remaining = reduceColoredComponent(remainingColored, entry.getKey(), entry.getValue());
            remaining = reduceColoredComponent(remainingPhyrexian, entry.getKey(), remaining);
            removeMatchingHybridSymbols(remainingHybrid, entry.getKey(), remaining);
        }
        for (Map.Entry<ManaColor, Integer> entry : reduction.phyrexianCosts.entrySet()) {
            int remaining = reduceColoredComponent(remainingColored, entry.getKey(), entry.getValue());
            remaining = reduceColoredComponent(remainingPhyrexian, entry.getKey(), remaining);
            removeMatchingHybridSymbols(remainingHybrid, entry.getKey(), remaining);
        }
        return new ManaCost(
                genericCost, remainingColored, remainingPhyrexian, remainingHybrid, snowCost,
                xSymbolCount, cumulativeUpkeepPayment);
    }

    private static int reduceColoredComponent(Map<ManaColor, Integer> components,
                                              ManaColor color, int reduction) {
        if (reduction <= 0) {
            return 0;
        }
        int matching = Math.min(components.getOrDefault(color, 0), reduction);
        if (matching > 0) {
            int remaining = components.get(color) - matching;
            if (remaining == 0) {
                components.remove(color);
            } else {
                components.put(color, remaining);
            }
        }
        return reduction - matching;
    }

    private static void removeMatchingHybridSymbols(List<HybridSymbol> hybrids,
                                                     ManaColor color, int reduction) {
        if (reduction <= 0) {
            return;
        }
        for (int i = hybrids.size() - 1; i >= 0 && reduction > 0; i--) {
            if (hybrids.get(i).colors().contains(color)) {
                hybrids.remove(i);
                reduction--;
            }
        }
    }

    /** Whether this cost can be paid after applying a mana-cost reduction. */
    public boolean canPayAfterReduction(ManaPool pool, ManaCost reduction) {
        return reducedBy(reduction).canPay(pool);
    }

    /** Pays this cost after applying a mana-cost reduction. */
    public void payAfterReduction(ManaPool pool, ManaCost reduction) {
        reducedBy(reduction).pay(pool);
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
     * Number of mana symbols that contain at least one of the given colors. A hybrid symbol counts
     * once for the combined color set even when it contains two colors from that set.
     */
    public int countSymbolsOfAnyColor(Set<ManaColor> colors) {
        int count = coloredCosts.entrySet().stream()
                .filter(entry -> colors.contains(entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
        count += phyrexianCosts.entrySet().stream()
                .filter(entry -> colors.contains(entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
        count += (int) hybridCosts.stream()
                .filter(hybrid -> hybrid.colors().stream().anyMatch(colors::contains))
                .count();
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
        int total = genericCost + snowCost;
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
        return !phyrexianCosts.isEmpty()
                || hybridCosts.stream().anyMatch(HybridSymbol::phyrexianAlternative);
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
        int totalPhyrexian = getPhyrexianManaCount();
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
        for (HybridSymbol hybrid : hybridCosts) {
            if (!hybrid.phyrexianAlternative()) {
                continue;
            }
            if (lifeSymbolsRemaining > 0) {
                lifeCost += 2;
                lifeSymbolsRemaining--;
            } else {
                ManaColor color = pickAvailableColor(hybrid.colors(), pool);
                if (color != null) {
                    pool.remove(color);
                } else if (autoMode) {
                    lifeCost += 2;
                }
            }
        }
        return lifeCost;
    }

    public int getPhyrexianManaCount() {
        return phyrexianCosts.values().stream().mapToInt(Integer::intValue).sum()
                + (int) hybridCosts.stream().filter(HybridSymbol::phyrexianAlternative).count();
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
        for (HybridSymbol hybrid : hybridCosts) {
            if (!hybrid.phyrexianAlternative()) {
                continue;
            }
            ManaColor chosen = null;
            for (ManaColor color : hybrid.colors()) {
                reserved.merge(color, 1, Integer::sum);
                if (canPayRestWithReserved(pool, xValue, reserved)) {
                    chosen = color;
                    break;
                }
                reserved.merge(color, -1, Integer::sum);
            }
            if (chosen == null) {
                lifeCost += 2;
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

    // ── White-as-red substitution (Sunglasses of Urza) ─────────────────
    // These only engage when the pool carries the whiteSpendableAsRed permission AND the cost needs
    // red, so the ordinary payment path (the overwhelming majority of casts) is left untouched.

    /** True if this cost requires red mana anywhere (a {R} pip, red hybrid, or red Phyrexian symbol). */
    private boolean requiresRed() {
        return countColorSymbols(ManaColor.RED) > 0;
    }

    /**
     * Affordability under the "spend white as red" permission: white may pay red pips. Modeled by
     * searching how many white mana are spent as red — for each candidate count {@code k}, convert
     * {@code k} white to red on a copy (with the permission cleared, so the plain path runs) and test
     * the supplied ordinary check. Payable iff some {@code k} works.
     */
    private static boolean canPayWithWhiteAsRed(ManaPool pool, Predicate<ManaPool> plainCheck) {
        return firstWhiteAsRedConversion(pool, plainCheck) >= 0;
    }

    /** Smallest number of white→red conversions that makes {@code plainCheck} pass, or -1 if none. */
    private static int firstWhiteAsRedConversion(ManaPool pool, Predicate<ManaPool> plainCheck) {
        int white = pool.get(ManaColor.WHITE);
        for (int k = 0; k <= white; k++) {
            ManaPool copy = new ManaPool(pool);
            copy.setWhiteSpendableAsRed(false);
            for (int i = 0; i < k; i++) {
                copy.remove(ManaColor.WHITE);
                copy.add(ManaColor.RED);
            }
            if (plainCheck.test(copy)) {
                return k;
            }
        }
        return -1;
    }

    /**
     * Prepares {@code pool} to be paid under the "spend white as red" permission: converts the fewest
     * white mana to red that keeps the ordinary check payable (so leftover white stays white), then
     * clears the permission on the pool so the ordinary payment path runs without re-entering here.
     */
    private static void applyWhiteAsRedForPayment(ManaPool pool, Predicate<ManaPool> plainCheck) {
        int k = firstWhiteAsRedConversion(pool, plainCheck);
        for (int i = 0; i < Math.max(0, k); i++) {
            pool.remove(ManaColor.WHITE);
            pool.add(ManaColor.RED);
        }
        pool.setWhiteSpendableAsRed(false);
    }

    /**
     * Rewrites {@code pool} under Celestial Dawn's permission — white mana may be spent as mana of
     * any color, every other mana only as though it were colorless. Every non-white colored mana
     * becomes colorless (which pays generic costs), then white is converted into exactly the colors
     * this cost's pips demand, cheapest first: one white per colored pip, then one per hybrid
     * symbol. Leftover white stays white, so it still pays {W} pips and generic. The permission is
     * cleared on the pool so the ordinary payment path runs without re-entering here.
     */
    private void applyWhiteAsAnyColor(ManaPool pool) {
        pool.setWhiteSpendableAsAnyColor(false);
        pool.setWhiteSpendableAsAnyColorWithoutRestriction(false);
        for (ManaColor color : ManaColor.values()) {
            if (color == ManaColor.WHITE || color == ManaColor.COLORLESS) {
                continue;
            }
            for (int amount = pool.get(color); amount > 0; amount--) {
                pool.remove(color);
                pool.add(ManaColor.COLORLESS);
            }
        }
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (entry.getKey() == ManaColor.WHITE || entry.getKey() == ManaColor.COLORLESS) {
                continue;
            }
            convertWhiteTo(pool, entry.getKey(), entry.getValue());
        }
        for (HybridSymbol hybrid : hybridCosts) {
            if (hybrid.colors().contains(ManaColor.WHITE)) {
                continue;
            }
            hybrid.colors().stream().findFirst().ifPresent(color -> convertWhiteTo(pool, color, 1));
        }
    }

    private void applySnowManaAsAnyColor(ManaPool pool) {
        pool.setSnowManaSpendableAsAnyColor(false);
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            ManaColor target = entry.getKey();
            if (target == ManaColor.COLORLESS) {
                continue;
            }
            int deficit = Math.max(0, entry.getValue() - pool.get(target));
            for (ManaColor source : ManaColor.values()) {
                if (source == target || deficit == 0) {
                    continue;
                }
                int tagged = pool.getSnowMana(source);
                int untagged = pool.get(source) - tagged;
                int nativeDemand = coloredCosts.getOrDefault(source, 0);
                int convertible = Math.max(0, tagged - Math.max(0, nativeDemand - untagged));
                int toConvert = Math.min(deficit, convertible);
                for (int i = 0; i < toConvert; i++) {
                    pool.convertSnowMana(source, target);
                }
                deficit -= toConvert;
            }
        }
    }

    /** Rewrites only the white mana needed for colored requirements, leaving other mana unchanged. */
    private void applyWhiteAsAnyColorWithoutRestriction(ManaPool pool) {
        pool.setWhiteSpendableAsAnyColorWithoutRestriction(false);
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (entry.getKey() == ManaColor.WHITE || entry.getKey() == ManaColor.COLORLESS) {
                continue;
            }
            convertWhiteTo(pool, entry.getKey(), entry.getValue());
        }
        for (HybridSymbol hybrid : hybridCosts) {
            if (hybrid.colors().contains(ManaColor.WHITE)) {
                continue;
            }
            hybrid.colors().stream().findFirst().ifPresent(color -> convertWhiteTo(pool, color, 1));
        }
    }

    /** Converts only the mana needed for this cost's colored requirements. */
    private void applyAllManaAsAnyColor(ManaPool pool) {
        pool.setAllManaSpendableAsAnyColor(false);
        pool.setWhiteSpendableAsRed(false);
        pool.setWhiteSpendableAsAnyColor(false);
        pool.setWhiteSpendableAsAnyColorWithoutRestriction(false);
        pool.setBlueSpendableAsAnyColorForActivatedAbilities(false);
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            convertAnyManaTo(pool, entry.getKey(), entry.getValue());
        }
        for (HybridSymbol hybrid : hybridCosts) {
            hybrid.colors().stream().findFirst().ifPresent(color -> convertAnyManaTo(pool, color, 1));
        }
    }

    /** Converts only the mana needed for an X color restriction after the fixed cost is handled. */
    private void applyAllManaAsAnyColor(ManaPool pool, Set<ManaColor> xColorRestrictions, int xDemand) {
        applyAllManaAsAnyColor(pool);
        ManaColor target = xColorRestrictions.stream().findFirst().orElse(null);
        if (target != null) {
            convertAnyManaTo(pool, target, xDemand);
        }
    }

    private static void convertAnyManaTo(ManaPool pool, ManaColor target, int count) {
        if (target == null || count <= 0) {
            return;
        }
        int remaining = Math.max(0, count - pool.get(target));
        for (ManaColor source : ManaColor.values()) {
            if (remaining <= 0 || source == target) {
                continue;
            }
            int amount = Math.min(remaining, pool.get(source));
            for (int i = 0; i < amount; i++) {
                pool.remove(source);
                pool.add(target);
            }
            remaining -= amount;
        }
    }

    /**
     * Finds a conversion of blue mana that makes the ordinary payment check succeed. Blue mana is
     * a wildcard only for colored requirements; leaving it blue is always preferable because blue
     * mana already pays generic costs and blue requirements.
     */
    private List<ManaColor> findBlueAsAnyColorPlan(ManaPool pool, Predicate<ManaPool> plainCheck) {
        ManaPool unchanged = copyManaPool(pool);
        unchanged.setBlueSpendableAsAnyColorForActivatedAbilities(false);
        if (plainCheck.test(unchanged)) {
            return List.of();
        }

        Set<ManaColor> conversionColors = EnumSet.noneOf(ManaColor.class);
        conversionColors.addAll(coloredCosts.keySet());
        for (HybridSymbol hybrid : hybridCosts) {
            conversionColors.addAll(hybrid.colors());
        }
        conversionColors.remove(ManaColor.BLUE);
        conversionColors.remove(ManaColor.COLORLESS);
        int maxConversions = Math.min(pool.get(ManaColor.BLUE), coloredCosts.values().stream()
                .mapToInt(Integer::intValue).sum() + phyrexianCosts.values().stream()
                .mapToInt(Integer::intValue).sum() + hybridCosts.size());
        List<ManaColor> colors = new ArrayList<>(conversionColors);
        for (int count = 1; count <= maxConversions; count++) {
            List<ManaColor> plan = new ArrayList<>(count);
            List<ManaColor> result = findBlueAsAnyColorPlan(pool, plainCheck, colors, 0, count, plan);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private List<ManaColor> findBlueAsAnyColorPlan(ManaPool pool, Predicate<ManaPool> plainCheck,
                                                    List<ManaColor> colors, int start, int remaining,
                                                    List<ManaColor> plan) {
        if (remaining == 0) {
            ManaPool candidate = copyManaPool(pool);
            for (ManaColor color : plan) {
                candidate.remove(ManaColor.BLUE);
                candidate.add(color);
            }
            candidate.setBlueSpendableAsAnyColorForActivatedAbilities(false);
            return plainCheck.test(candidate) ? new ArrayList<>(plan) : null;
        }
        for (int i = start; i < colors.size(); i++) {
            plan.add(colors.get(i));
            List<ManaColor> result = findBlueAsAnyColorPlan(pool, plainCheck, colors, i, remaining - 1, plan);
            plan.removeLast();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static ManaPool copyManaPool(ManaPool pool) {
        return pool instanceof VirtualManaPool virtual
                ? new VirtualManaPool(virtual)
                : new ManaPool(pool);
    }

    private ManaCost withoutSnowCost() {
        return new ManaCost(this, 0);
    }

    private boolean canPayWithBlueAsAnyColor(ManaPool pool, Predicate<ManaPool> plainCheck) {
        return findBlueAsAnyColorPlan(pool, plainCheck) != null;
    }

    private void applyBlueAsAnyColorForPayment(ManaPool pool, Predicate<ManaPool> plainCheck) {
        List<ManaColor> plan = findBlueAsAnyColorPlan(pool, plainCheck);
        if (plan != null) {
            for (ManaColor color : plan) {
                pool.remove(ManaColor.BLUE);
                pool.add(color);
            }
        }
        pool.setBlueSpendableAsAnyColorForActivatedAbilities(false);
    }

    /** Converts up to {@code count} white mana in {@code pool} into {@code color}. */
    private static void convertWhiteTo(ManaPool pool, ManaColor color, int count) {
        for (int i = 0; i < count && pool.get(ManaColor.WHITE) > 0; i++) {
            pool.remove(ManaColor.WHITE);
            pool.add(color);
        }
    }

    public boolean canPay(ManaPool pool) {
        return canPay(pool, 0);
    }

    public boolean canPayForForetell(ManaPool pool) {
        return canPay(pool.copyForForetellPayment(), 0, false, false, false, false, true);
    }

    public boolean canPayForDisturb(ManaPool pool, int xValue, int additionalGenericCost) {
        ManaPool paymentPool = pool.copyForDisturbPayment();
        return canPayWithAdditionalGenericCost(paymentPool, xValue, additionalGenericCost,
                false, false, false, false, true);
    }

    /** Checks payment for a spell cast from a graveyard, including graveyard-only mana. */
    public boolean canPayFromGraveyard(ManaPool pool, int xValue, int additionalGenericCost) {
        ManaPool paymentPool = copyManaPool(pool);
        ManaPool.GraveyardOnlyManaState state = paymentPool.promoteGraveyardOnlyMana();
        try {
            return canPayWithAdditionalGenericCost(paymentPool, xValue, additionalGenericCost);
        } finally {
            paymentPool.restorePromotedGraveyardOnlyMana(state);
        }
    }

    public boolean canPayFromGraveyard(ManaPool pool, int additionalGenericCost) {
        return canPayFromGraveyard(pool, 0, additionalGenericCost);
    }

    /** Checks a disturb-style graveyard cast using graveyard-only mana. */
    public boolean canPayForDisturbFromGraveyard(ManaPool pool, int xValue, int additionalGenericCost) {
        ManaPool paymentPool = pool.copyForDisturbPayment();
        ManaPool.GraveyardOnlyManaState state = paymentPool.promoteGraveyardOnlyMana();
        try {
            return canPayWithAdditionalGenericCost(paymentPool, xValue, additionalGenericCost,
                    false, false, false, false, true);
        } finally {
            paymentPool.restorePromotedGraveyardOnlyMana(state);
        }
    }

    public boolean canPay(ManaPool pool, int xValue) {
        if (snowCost > 0) {
            if (pool.getSnowManaTotal() < snowCost) {
                return false;
            }
            ManaPool remaining = copyManaPool(pool);
            remaining.removeSnowMana(snowCost);
            return withoutSnowCost().canPay(remaining, xValue);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            ManaPool rewritten = new ManaPool(pool);
            applySnowManaAsAnyColor(rewritten);
            return canPay(rewritten, xValue);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applyAllManaAsAnyColor(rewritten);
            return canPay(rewritten, xValue);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            return canPayWithWhiteAsRed(pool, p -> canPay(p, xValue));
        }
        if (pool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
            return canPayWithBlueAsAnyColor(pool, p -> canPay(p, xValue));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColor(rewritten);
            return canPay(rewritten, xValue);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColorWithoutRestriction(rewritten);
            return canPay(rewritten, xValue);
        }
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

    /** Checks payment with a chosen X value and an independent generic-cost modifier. */
    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost) {
        if (snowCost > 0) {
            if (pool.getSnowManaTotal() < snowCost) {
                return false;
            }
            ManaPool remaining = copyManaPool(pool);
            remaining.removeSnowMana(snowCost);
            return withoutSnowCost().canPayWithAdditionalGenericCost(
                    remaining, xValue, additionalGenericCost);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            ManaPool rewritten = new ManaPool(pool);
            applySnowManaAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applyAllManaAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            return canPayWithWhiteAsRed(pool,
                    p -> canPayWithAdditionalGenericCost(p, xValue, additionalGenericCost));
        }
        if (pool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
            return canPayWithBlueAsAnyColor(pool,
                    p -> canPayWithAdditionalGenericCost(p, xValue, additionalGenericCost));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColorWithoutRestriction(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost);
        }
        Map<ManaColor, Integer> available = availableByColor(pool);
        if (!reserveColoredCosts(available)) {
            return false;
        }
        int[] extraGeneric = {0};
        if (!assignHybrids(available, extraGeneric)) {
            return false;
        }
        int remaining = totalOf(available) - residualFlexibleOvercount(pool) + xCostOnlyAvailable(pool);
        int xDemand = hasX() ? xValue * effectiveXMultiplier() : 0;
        return remaining >= genericCost + extraGeneric[0] + xDemand + additionalGenericCost;
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

    private int artifactOnlyManaUsedForColoredCosts(ManaPool pool) {
        int used = 0;
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int need = Math.max(0, entry.getValue() - pool.get(entry.getKey()));
            used += Math.min(need, pool.getArtifactOnlyMana(entry.getKey()));
        }
        return used;
    }

    private int artifactSpellOrAbilityOnlyManaUsedForColoredCosts(ManaPool pool) {
        int used = 0;
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int need = Math.max(0, entry.getValue() - pool.get(entry.getKey()));
            need = Math.max(0, need - pool.getArtifactOnlyMana(entry.getKey()));
            used += Math.min(need, pool.getArtifactSpellOrAbilityOnlyMana(entry.getKey()));
        }
        return used;
    }

    // ── Hybrid mana support (shared by the core canPay/pay path) ───────

    private Map<ManaColor, Integer> availableByColor(ManaPool pool) {
        return availableByColor(pool, false);
    }

    private Map<ManaColor, Integer> availableByColor(ManaPool pool, boolean artifactContext) {
        Map<ManaColor, Integer> available = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int amount = pool.get(color);
            if (artifactContext) {
                amount += pool.getArtifactOnlyMana(color);
                amount += pool.getArtifactSpellOrAbilityOnlyMana(color);
            }
            if (cumulativeUpkeepPayment) {
                if (color == ManaColor.COLORLESS) {
                    amount += pool.getCumulativeUpkeepOnlyColorless();
                } else {
                    amount += pool.getCumulativeUpkeepOnlyColored(color);
                }
            }
            available.put(color, amount);
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
        List<HybridSymbol> sorted = hybridCosts.stream()
                .filter(hybrid -> !hybrid.phyrexianAlternative())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
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

    private static ManaColor pickAvailableColor(Set<ManaColor> colors, ManaPool pool) {
        return colors.stream()
                .max(Comparator.comparingInt(pool::get))
                .filter(color -> pool.get(color) > 0)
                .orElse(null);
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
        if (snowCost > 0) {
            if (pool.getSnowManaTotal() < snowCost) {
                return false;
            }
            ManaPool remaining = copyManaPool(pool);
            remaining.removeSnowMana(snowCost);
            return withoutSnowCost().canPay(remaining, xValue, artifactContext, myrContext,
                    restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applyAllManaAsAnyColor(rewritten);
            return canPay(rewritten, xValue, artifactContext, myrContext, restrictedRedContext,
                    kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            return canPayWithWhiteAsRed(pool, p -> canPay(p, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColor(rewritten);
            return canPay(rewritten, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColorWithoutRestriction(rewritten);
            return canPay(rewritten, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext);
        }
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int available = pool.get(entry.getKey());
            if (artifactContext) {
                available += pool.getArtifactOnlyMana(entry.getKey());
                available += pool.getArtifactSpellOrAbilityOnlyMana(entry.getKey());
            }
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
            remaining -= Math.min(entry.getValue(), pool.get(entry.getKey()));
        }

        if (artifactContext) {
            remaining += pool.getArtifactOnlyColorless();
            remaining += pool.getArtifactOnlyManaTotal() - artifactOnlyManaUsedForColoredCosts(pool);
            remaining += pool.getArtifactSpellOrAbilityOnlyManaTotal()
                    - artifactSpellOrAbilityOnlyManaUsedForColoredCosts(pool);
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
                if (artifactContext) {
                    amount += pool.getArtifactOnlyMana(color);
                    amount += pool.getArtifactSpellOrAbilityOnlyMana(color);
                }
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

    /** Checks payment with an independent generic-cost modifier under mana restrictions. */
    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                                    boolean artifactContext, boolean myrContext,
                                                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                                    boolean instantSorceryOnlyColorlessContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, false);
    }

    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                                    boolean artifactContext, boolean myrContext,
                                                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                                    boolean instantSorceryOnlyColorlessContext,
                                                    boolean powerstoneContext) {
        if (pool.isAllManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applyAllManaAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, powerstoneContext);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            return canPayWithWhiteAsRed(pool, p -> canPayWithAdditionalGenericCost(p, xValue,
                    additionalGenericCost, artifactContext, myrContext, restrictedRedContext,
                    kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, powerstoneContext));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, powerstoneContext);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColorWithoutRestriction(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, powerstoneContext);
        }
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int available = pool.get(entry.getKey());
            if (artifactContext) {
                available += pool.getArtifactSpellOrAbilityOnlyMana(entry.getKey());
            }
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
            remaining -= Math.min(entry.getValue(), pool.get(entry.getKey()));
        }
        if (artifactContext) {
            remaining += pool.getArtifactOnlyColorless();
            remaining += pool.getArtifactSpellOrAbilityOnlyManaTotal()
                    - artifactSpellOrAbilityOnlyManaUsedForColoredCosts(pool);
        }
        if (powerstoneContext) {
            remaining += pool.getPowerstoneOnlyColorless();
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
                if (artifactContext) {
                    amount += pool.getArtifactSpellOrAbilityOnlyMana(color);
                }
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

        return remaining >= genericCost + hybridGeneric + xValue * effectiveXMultiplier()
                + additionalGenericCost;
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, null);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext, Set.of());
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext, false,
                subtypeOrPlaneswalkerSpellContext);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, boolean manaValueAtLeastFourContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext,
                manaValueAtLeastFourContext, Set.of());
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, boolean manaValueAtLeastFourContext, Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext) {
        return canPay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext,
                manaValueAtLeastFourContext, subtypeOrPlaneswalkerSpellContext, false);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, boolean manaValueAtLeastFourContext, Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext, boolean powerstoneContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, powerstoneContext);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext,
                          boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                          boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                          Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                          boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext,
                          boolean manaValueAtLeastFourContext,
                          Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                          boolean powerstoneContext, Set<CardSubtype> subtypeSpellOnlyContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, Set.of(), powerstoneContext, subtypeSpellOnlyContext);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext,
                          boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                          boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                          Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                          boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext,
                          boolean manaValueAtLeastFourContext,
                          Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                          Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                          boolean powerstoneContext, Set<CardSubtype> subtypeSpellOnlyContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                powerstoneContext, subtypeSpellOnlyContext);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext,
                           boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                           boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                           Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                           boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext,
                           boolean manaValueAtLeastFourContext,
                           Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                           Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext);
    }

    public boolean canPay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext,
                          boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                          boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                          Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                          boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext,
                          boolean manaValueAtLeastFourContext,
                          Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                          Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                          boolean powerstoneContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                powerstoneContext);
    }

    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                                    boolean artifactContext, boolean myrContext,
                                                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                                    boolean instantSorceryOnlyColorlessContext,
                                                    Set<CardSubtype> subtypeCreatureContext,
                                                    Set<CardSubtype> subtypeSpellOrAbilityContext,
                                                    boolean creatureSpellOnlyContext,
                                                    boolean artifactAbilityOnlyContext,
                                                    boolean legendarySpellOnlyContext,
                                                    boolean manaValueAtLeastFourContext,
                                                    Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, Set.of(), false);
    }

    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                                    boolean artifactContext, boolean myrContext,
                                                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                                    boolean instantSorceryOnlyColorlessContext,
                                                    Set<CardSubtype> subtypeCreatureContext,
                                                    Set<CardSubtype> subtypeSpellOrAbilityContext,
                                                    boolean creatureSpellOnlyContext,
                                                    boolean artifactAbilityOnlyContext,
                                                    boolean legendarySpellOnlyContext,
                                                    boolean manaValueAtLeastFourContext,
                                                    Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                                    Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext, false);
    }

    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                                    boolean artifactContext, boolean myrContext,
                                                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                                    boolean instantSorceryOnlyColorlessContext,
                                                    Set<CardSubtype> subtypeCreatureContext,
                                                    Set<CardSubtype> subtypeSpellOrAbilityContext,
                                                    boolean creatureSpellOnlyContext,
                                                    boolean artifactAbilityOnlyContext,
                                                    boolean legendarySpellOnlyContext,
                                                    boolean manaValueAtLeastFourContext,
                                                    Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                                    boolean powerstoneContext) {
        return canPayWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, Set.of(), powerstoneContext);
    }

    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                                    boolean artifactContext, boolean myrContext,
                                                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                                    boolean instantSorceryOnlyColorlessContext,
                                                    Set<CardSubtype> subtypeCreatureContext,
                                                    Set<CardSubtype> subtypeSpellOrAbilityContext,
                                                    boolean creatureSpellOnlyContext,
                                                    boolean artifactAbilityOnlyContext,
                                                    boolean legendarySpellOnlyContext,
                                                    boolean manaValueAtLeastFourContext,
                                                    Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                                    Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                                                    boolean powerstoneContext) {
        if (snowCost > 0) {
            if (pool.getSnowManaTotal() < snowCost) {
                return false;
            }
            ManaPool remaining = copyManaPool(pool);
            remaining.removeSnowMana(snowCost);
            return withoutSnowCost().canPayWithAdditionalGenericCost(
                    remaining, xValue, additionalGenericCost, artifactContext, myrContext,
                    restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                    subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                    artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applySnowManaAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                    artifactAbilityOnlyContext, legendarySpellOnlyContext,
                    manaValueAtLeastFourContext, subtypeOrPlaneswalkerSpellContext,
                    subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext);
        }
        return canPayWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                powerstoneContext, Set.of());
    }

    public boolean canPayWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                                    boolean artifactContext, boolean myrContext,
                                                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                                    boolean instantSorceryOnlyColorlessContext,
                                                    Set<CardSubtype> subtypeCreatureContext,
                                                    Set<CardSubtype> subtypeSpellOrAbilityContext,
                                                    boolean creatureSpellOnlyContext,
                                                    boolean artifactAbilityOnlyContext,
                                                    boolean legendarySpellOnlyContext,
                                                    boolean manaValueAtLeastFourContext,
                                                    Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                                    Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                                                    boolean powerstoneContext,
                                                    Set<CardSubtype> subtypeSpellOnlyContext) {
        if (snowCost > 0) {
            if (pool.getSnowManaTotal() < snowCost) {
                return false;
            }
            ManaPool remaining = copyManaPool(pool);
            remaining.removeSnowMana(snowCost);
            return withoutSnowCost().canPayWithAdditionalGenericCost(
                    remaining, xValue, additionalGenericCost, artifactContext, myrContext,
                    restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                    subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                    artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext, subtypeSpellOnlyContext);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applySnowManaAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                    artifactAbilityOnlyContext, legendarySpellOnlyContext,
                    manaValueAtLeastFourContext, subtypeOrPlaneswalkerSpellContext,
                    subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext,
                    subtypeSpellOnlyContext);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applyAllManaAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                    artifactAbilityOnlyContext, legendarySpellOnlyContext,
                    manaValueAtLeastFourContext, subtypeOrPlaneswalkerSpellContext,
                    subtypeCreatureSourceSpellOrAbilityContext, powerstoneContext, subtypeSpellOnlyContext);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            return canPayWithWhiteAsRed(pool, p -> canPayWithAdditionalGenericCost(p, xValue,
                    additionalGenericCost, artifactContext, myrContext, restrictedRedContext,
                    kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext,
                    legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext, subtypeSpellOnlyContext));
        }
        if (pool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
            return canPayWithBlueAsAnyColor(pool, p -> canPayWithAdditionalGenericCost(p, xValue,
                    additionalGenericCost, artifactContext, myrContext, restrictedRedContext,
                    kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext,
                    legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext, subtypeSpellOnlyContext));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColor(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext,
                    legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext, subtypeSpellOnlyContext);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            ManaPool rewritten = new ManaPool(pool);
            applyWhiteAsAnyColorWithoutRestriction(rewritten);
            return canPayWithAdditionalGenericCost(rewritten, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext,
                    legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext);
        }
        boolean hasCreatureCtx = subtypeCreatureContext != null && !subtypeCreatureContext.isEmpty();
        boolean hasSpellOrAbilityCtx = subtypeSpellOrAbilityContext != null && !subtypeSpellOrAbilityContext.isEmpty();
        boolean hasCreatureSourceSpellOrAbilityCtx = subtypeCreatureSourceSpellOrAbilityContext != null
                && !subtypeCreatureSourceSpellOrAbilityContext.isEmpty();
        boolean hasSubtypeOrPlaneswalkerSpellCtx = subtypeOrPlaneswalkerSpellContext != null
                && !subtypeOrPlaneswalkerSpellContext.isEmpty();
        boolean hasSpellOnlyCtx = subtypeSpellOnlyContext != null && !subtypeSpellOnlyContext.isEmpty();
        if (!hasCreatureCtx && !hasSpellOrAbilityCtx && !hasCreatureSourceSpellOrAbilityCtx
                && !hasSubtypeOrPlaneswalkerSpellCtx
                && !creatureSpellOnlyContext && !artifactAbilityOnlyContext && !legendarySpellOnlyContext
                && !manaValueAtLeastFourContext && !powerstoneContext && !hasSpellOnlyCtx) {
            return canPayWithAdditionalGenericCost(pool, xValue, additionalGenericCost,
                    artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, powerstoneContext);
        }
        Set<CardSubtype> creatureCtx = hasCreatureCtx ? subtypeCreatureContext : Set.of();
        Set<CardSubtype> soaCtx = hasSpellOrAbilityCtx ? subtypeSpellOrAbilityContext : Set.of();
        Set<CardSubtype> creatureSourceSoaCtx = hasCreatureSourceSpellOrAbilityCtx
                ? subtypeCreatureSourceSpellOrAbilityContext : Set.of();
        Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerCtx =
                hasSubtypeOrPlaneswalkerSpellCtx ? subtypeOrPlaneswalkerSpellContext : Set.of();
        Set<CardSubtype> spellOnlyCtx = hasSpellOnlyCtx ? subtypeSpellOnlyContext : Set.of();
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        // Check each colored cost can be paid from combined sources
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            int available = pool.get(entry.getKey());
            if (artifactContext) {
                available += pool.getArtifactOnlyMana(entry.getKey());
                available += pool.getArtifactSpellOrAbilityOnlyMana(entry.getKey());
            }
            available += pool.getSubtypeCreatureManaForColor(creatureCtx, entry.getKey());
            available += pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, entry.getKey());
            available += pool.getSubtypeSpellOnlyManaForColor(spellOnlyCtx, entry.getKey());
            available += pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(
                    creatureSourceSoaCtx, entry.getKey());
            available += pool.getSubtypeOrPlaneswalkerSpellManaForColor(subtypeOrPlaneswalkerCtx, entry.getKey());
            if (creatureSpellOnlyContext) {
                available += pool.getCreatureSpellOnlyMana(entry.getKey());
            }
            if (manaValueAtLeastFourContext) {
                available += pool.getManaValueAtLeastFourOnlyMana(entry.getKey());
            }
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
            totalUsable += pool.getArtifactOnlyManaTotal();
            totalUsable += pool.getArtifactSpellOrAbilityOnlyManaTotal();
        }
        if (artifactAbilityOnlyContext) {
            totalUsable += pool.getArtifactAbilityOnlyColorless();
        }
        if (powerstoneContext) {
            totalUsable += pool.getPowerstoneOnlyColorless();
        }
        if (legendarySpellOnlyContext) {
            totalUsable += pool.getLegendarySpellOnlyColorless();
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
        totalUsable += pool.getSubtypeSpellOnlyManaTotal(spellOnlyCtx);
        totalUsable += pool.getSubtypeCreatureSourceSpellOrAbilityManaTotal(creatureSourceSoaCtx);
        totalUsable += pool.getSubtypeOrPlaneswalkerSpellManaTotal(subtypeOrPlaneswalkerCtx);
        if (creatureSpellOnlyContext) {
            totalUsable += pool.getCreatureSpellOnlyManaTotal();
        }
        if (manaValueAtLeastFourContext) {
            totalUsable += pool.getManaValueAtLeastFourOnlyManaTotal();
        }
        totalUsable += xCostOnlyAvailable(pool);

        int hybridGeneric = 0;
        if (!hybridCosts.isEmpty()) {
            Map<ManaColor, Integer> available = new EnumMap<>(ManaColor.class);
            for (ManaColor color : ManaColor.values()) {
                int amount = pool.get(color);
                if (artifactContext) {
                    amount += pool.getArtifactOnlyMana(color);
                    amount += pool.getArtifactSpellOrAbilityOnlyMana(color);
                }
                if (powerstoneContext && color == ManaColor.COLORLESS) {
                    amount += pool.getPowerstoneOnlyColorless();
                }
                amount += pool.getSubtypeCreatureManaForColor(creatureCtx, color);
                amount += pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, color);
                amount += pool.getSubtypeSpellOnlyManaForColor(spellOnlyCtx, color);
                amount += pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(creatureSourceSoaCtx, color);
                amount += pool.getSubtypeOrPlaneswalkerSpellManaForColor(subtypeOrPlaneswalkerCtx, color);
                if (creatureSpellOnlyContext) {
                    amount += pool.getCreatureSpellOnlyMana(color);
                }
                if (manaValueAtLeastFourContext) {
                    amount += pool.getManaValueAtLeastFourOnlyMana(color);
                }
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

        return totalUsable - totalColored >= genericCost + hybridGeneric
                + xValue * effectiveXMultiplier() + additionalGenericCost;
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

    /** Checks a flashback-style graveyard cast using both restricted graveyard mana buckets. */
    public boolean canPayFlashbackFromGraveyard(ManaPool pool, int xValue) {
        ManaPool paymentPool = copyManaPool(pool);
        ManaPool.GraveyardOnlyManaState state = paymentPool.promoteGraveyardOnlyMana();
        try {
            return canPayFlashback(paymentPool, xValue);
        } finally {
            paymentPool.restorePromotedGraveyardOnlyMana(state);
        }
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

    /** Pays a spell cast from a graveyard, allowing graveyard-only mana for its cost. */
    public void payFromGraveyard(ManaPool pool, int xValue, int additionalGenericCost) {
        ManaPool.GraveyardOnlyManaState state = pool.promoteGraveyardOnlyMana();
        try {
            payWithAdditionalGenericCost(pool, xValue, additionalGenericCost);
        } finally {
            pool.restorePromotedGraveyardOnlyMana(state);
        }
    }

    public void payFromGraveyard(ManaPool pool, int additionalGenericCost) {
        payFromGraveyard(pool, 0, additionalGenericCost);
    }

    /** Pays a flashback-style graveyard cast using both restricted graveyard mana buckets. */
    public void payFlashbackFromGraveyard(ManaPool pool, int xValue) {
        ManaPool.GraveyardOnlyManaState state = pool.promoteGraveyardOnlyMana();
        try {
            payFlashback(pool, xValue);
        } finally {
            pool.restorePromotedGraveyardOnlyMana(state);
        }
    }

    public boolean canPay(ManaPool pool, int xValue, ManaColor xColorRestriction, int additionalGenericCost) {
        return canPay(pool, xValue, java.util.EnumSet.of(xColorRestriction), additionalGenericCost);
    }

    /**
     * Like {@link #canPay(ManaPool, int, ManaColor, int)} but X may be paid with any mix of the
     * allowed colors (Soul Burn: black and/or red).
     */
    public boolean canPay(ManaPool pool, int xValue, Set<ManaColor> xColorRestrictions, int additionalGenericCost) {
        if (pool.isAllManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applyAllManaAsAnyColor(rewritten, xColorRestrictions, xValue * xSymbolCount);
            return canPay(rewritten, xValue, xColorRestrictions, additionalGenericCost);
        }
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        int restrictedAvailable = 0;
        for (ManaColor color : xColorRestrictions) {
            int available = pool.get(color);
            if (coloredCosts.containsKey(color)) {
                available -= coloredCosts.get(color);
            }
            restrictedAvailable += Math.max(0, available);
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
        return calculateMaxX(pool, 0);
    }

    /**
     * Calculates the maximum X value that can be paid with the given mana pool and an independent
     * generic-cost modifier.
     */
    public int calculateMaxX(ManaPool pool, int additionalGenericCost) {
        if (xSymbolCount <= 0) {
            return 0;
        }
        int low = 0;
        int high = Math.max(0, pool.getTotal() + pool.getXCostOnlyColorless()
                - Math.min(0, additionalGenericCost));
        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            if (canPayWithAdditionalGenericCost(pool, candidate, additionalGenericCost)) {
                low = candidate;
            } else {
                high = candidate - 1;
            }
        }
        return low;
    }

    /**
     * Calculates the maximum X value that can be paid with the given mana pool
     * when X must be paid with a specific color (e.g., Consume Spirit requires {B} for X).
     * Returns 0 if the base cost cannot be paid.
     */
    public int calculateMaxX(ManaPool pool, ManaColor xColorRestriction, int additionalGenericCost) {
        return calculateMaxX(pool, java.util.EnumSet.of(xColorRestriction), additionalGenericCost);
    }

    /**
     * Max X when X must be paid with any mix of the allowed colors.
     */
    public int calculateMaxX(ManaPool pool, Set<ManaColor> xColorRestrictions, int additionalGenericCost) {
        if (xSymbolCount <= 0) {
            return 0;
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            ManaPool rewritten = copyManaPool(pool);
            applyAllManaAsAnyColor(rewritten, xColorRestrictions, 0);
            ManaColor target = xColorRestrictions.stream().findFirst().orElse(null);
            if (target != null) {
                convertAnyManaTo(rewritten, target, rewritten.getTotal());
            }
            return calculateMaxX(rewritten, xColorRestrictions, additionalGenericCost);
        }
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            if (pool.get(entry.getKey()) < entry.getValue()) {
                return 0;
            }
        }

        int restrictedAvailable = 0;
        for (ManaColor color : xColorRestrictions) {
            int available = pool.get(color);
            if (coloredCosts.containsKey(color)) {
                available -= coloredCosts.get(color);
            }
            restrictedAvailable += Math.max(0, available);
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

    public void payForForetell(ManaPool pool) {
        ManaPool.ForetellPaymentState state = pool.beginForetellPayment();
        try {
            pay(pool, 0, false, false, false, false, true);
        } finally {
            pool.endForetellPayment(state);
        }
    }

    public void payForDisturb(ManaPool pool, int xValue, int additionalGenericCost) {
        ManaPool.DisturbPaymentState state = pool.beginDisturbPayment();
        try {
            payWithAdditionalGenericCost(pool, xValue, additionalGenericCost,
                    false, false, false, false, true);
        } finally {
            pool.endDisturbPayment(state);
        }
    }

    /** Pays a disturb-style graveyard cast using graveyard-only mana. */
    public void payForDisturbFromGraveyard(ManaPool pool, int xValue, int additionalGenericCost) {
        ManaPool.GraveyardOnlyManaState state = pool.promoteGraveyardOnlyMana();
        try {
            payForDisturb(pool, xValue, additionalGenericCost);
        } finally {
            pool.restorePromotedGraveyardOnlyMana(state);
        }
    }

    public void pay(ManaPool pool, int xValue) {
        if (snowCost > 0) {
            pool.removeSnowMana(snowCost);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            applySnowManaAsAnyColor(pool);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            applyAllManaAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            applyWhiteAsRedForPayment(pool, p -> canPay(p, xValue));
        }
        if (pool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
            applyBlueAsAnyColorForPayment(pool, p -> canPay(p, xValue));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            applyWhiteAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            applyWhiteAsAnyColorWithoutRestriction(pool);
        }
        if (cumulativeUpkeepPayment) {
            payWithCumulativeUpkeepMana(pool, xValue);
            return;
        }
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

    /** Pays with a chosen X value and an independent generic-cost modifier. */
    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost) {
        if (snowCost > 0) {
            pool.removeSnowMana(snowCost);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            applySnowManaAsAnyColor(pool);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            applyAllManaAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            applyWhiteAsRedForPayment(pool,
                    p -> canPayWithAdditionalGenericCost(p, xValue, additionalGenericCost));
        }
        if (pool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
            applyBlueAsAnyColorForPayment(pool,
                    p -> canPayWithAdditionalGenericCost(p, xValue, additionalGenericCost));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            applyWhiteAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            applyWhiteAsAnyColorWithoutRestriction(pool);
        }
        if (cumulativeUpkeepPayment) {
            payWithCumulativeUpkeepMana(pool, xValue, additionalGenericCost);
            return;
        }
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }

        int extraHybridGeneric = payHybrids(pool);
        int xDemand = hasX() ? xValue * effectiveXMultiplier() : 0;
        int remainingGeneric = genericCost + extraHybridGeneric + xDemand + additionalGenericCost;
        remainingGeneric = spendXCostOnlyForGeneric(pool, remainingGeneric);
        payGenericPreferColorless(pool, remainingGeneric);
    }

    /**
     * Pays this cost using cumulative-upkeep-only mana first (colored then colorless), then regular
     * pool mana. Used when {@link #cumulativeUpkeepPayment} is true.
     */
    private void payWithCumulativeUpkeepMana(ManaPool pool, int xValue) {
        payWithCumulativeUpkeepMana(pool, xValue, 0);
    }

    private void payWithCumulativeUpkeepMana(ManaPool pool, int xValue, int additionalGenericCost) {
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            ManaColor color = entry.getKey();
            int needed = entry.getValue();
            int fromCu = Math.min(needed, pool.getCumulativeUpkeepOnlyColored(color));
            pool.removeCumulativeUpkeepOnlyColored(color, fromCu);
            needed -= fromCu;
            for (int i = 0; i < needed; i++) {
                pool.remove(color);
            }
        }

        int extraHybridGeneric = payHybridsPreferringCumulativeUpkeep(pool);

        int xDemand = hasX() ? xValue * effectiveXMultiplier() : 0;
        int remainingGeneric = genericCost + extraHybridGeneric + xDemand + additionalGenericCost;
        int fromCuColorless = Math.min(remainingGeneric, pool.getCumulativeUpkeepOnlyColorless());
        pool.removeCumulativeUpkeepOnlyColorless(fromCuColorless);
        remainingGeneric -= fromCuColorless;
        for (ManaColor color : ManaColor.values()) {
            if (color == ManaColor.COLORLESS || remainingGeneric <= 0) {
                continue;
            }
            int fromCu = Math.min(remainingGeneric, pool.getCumulativeUpkeepOnlyColored(color));
            pool.removeCumulativeUpkeepOnlyColored(color, fromCu);
            remainingGeneric -= fromCu;
        }
        remainingGeneric = spendXCostOnlyForGeneric(pool, remainingGeneric);
        payGenericPreferColorless(pool, remainingGeneric);
    }

    /**
     * Like {@link #payHybrids} but prefers spending cumulative-upkeep-only colored mana when assigning
     * hybrid symbols.
     */
    private int payHybridsPreferringCumulativeUpkeep(ManaPool pool) {
        if (hybridCosts.isEmpty()) {
            return 0;
        }
        Map<ManaColor, Integer> available = availableByColor(pool);
        Map<ManaColor, Integer> before = new EnumMap<>(available);
        int[] extraGeneric = {0};
        assignHybrids(available, extraGeneric);
        for (ManaColor color : ManaColor.values()) {
            int spent = before.get(color) - available.get(color);
            if (spent <= 0) {
                continue;
            }
            if (color == ManaColor.COLORLESS) {
                int fromCu = Math.min(spent, pool.getCumulativeUpkeepOnlyColorless());
                pool.removeCumulativeUpkeepOnlyColorless(fromCu);
                spent -= fromCu;
                for (int i = 0; i < spent; i++) {
                    pool.remove(ManaColor.COLORLESS);
                }
            } else {
                int fromCu = Math.min(spent, pool.getCumulativeUpkeepOnlyColored(color));
                pool.removeCumulativeUpkeepOnlyColored(color, fromCu);
                spent -= fromCu;
                for (int i = 0; i < spent; i++) {
                    pool.remove(color);
                }
            }
        }
        return extraGeneric[0];
    }

    /**
     * Spends colored mana for the hybrid symbols (after fixed colored costs are already paid) and
     * returns the additional generic mana owed for any monocolored hybrids paid via their generic
     * alternative. Assumes the cost is affordable (callers gate on {@link #canPay}).
     */
    private int payHybrids(ManaPool pool) {
        return payHybrids(pool, false);
    }

    private int payHybrids(ManaPool pool, boolean artifactContext) {
        if (hybridCosts.isEmpty()) {
            return 0;
        }
        Map<ManaColor, Integer> available = availableByColor(pool, artifactContext);
        Map<ManaColor, Integer> before = new EnumMap<>(available);
        int[] extraGeneric = {0};
        assignHybrids(available, extraGeneric);
        for (ManaColor color : ManaColor.values()) {
            int spent = before.get(color) - available.get(color);
            if (artifactContext && spent > 0) {
                int fromArtifact = Math.min(spent, pool.getArtifactOnlyMana(color));
                pool.removeArtifactOnlyMana(color, fromArtifact);
                spent -= fromArtifact;
                int fromGuidelight = Math.min(spent, pool.getArtifactSpellOrAbilityOnlyMana(color));
                pool.removeArtifactSpellOrAbilityOnlyMana(color, fromGuidelight);
                spent -= fromGuidelight;
            }
            for (int i = 0; i < spent; i++) {
                pool.remove(color);
            }
        }
        return extraGeneric[0];
    }

    private int payHybrids(ManaPool pool, boolean artifactContext, boolean restrictedRedContext,
                            boolean kickedOnlyGreenContext,
                            boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                            Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                            Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                            Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext) {
        return payHybrids(pool, artifactContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, subtypeOrPlaneswalkerSpellContext,
                subtypeCreatureSourceSpellOrAbilityContext, Set.of());
    }

    private int payHybrids(ManaPool pool, boolean artifactContext, boolean restrictedRedContext,
                            boolean kickedOnlyGreenContext,
                            boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                            Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                            Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                            Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                            Set<CardSubtype> subtypeSpellOnlyContext) {
        if (hybridCosts.isEmpty()) {
            return 0;
        }
        Set<CardSubtype> creatureCtx = subtypeCreatureContext == null ? Set.of() : subtypeCreatureContext;
        Set<CardSubtype> soaCtx = subtypeSpellOrAbilityContext == null ? Set.of() : subtypeSpellOrAbilityContext;
        Set<CardSubtype> creatureSourceSoaCtx = subtypeCreatureSourceSpellOrAbilityContext == null
                ? Set.of() : subtypeCreatureSourceSpellOrAbilityContext;
        Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerCtx =
                subtypeOrPlaneswalkerSpellContext == null ? Set.of() : subtypeOrPlaneswalkerSpellContext;
        Set<CardSubtype> spellOnlyCtx = subtypeSpellOnlyContext == null ? Set.of() : subtypeSpellOnlyContext;
        Map<ManaColor, Integer> available = new EnumMap<>(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            int amount = pool.get(color)
                    + pool.getSubtypeCreatureManaForColor(creatureCtx, color)
                    + pool.getSubtypeSpellOnlyManaForColor(spellOnlyCtx, color)
                    + pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, color)
                    + pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(creatureSourceSoaCtx, color)
                    + pool.getSubtypeOrPlaneswalkerSpellManaForColor(subtypeOrPlaneswalkerCtx, color);
            if (artifactContext) {
                amount += pool.getArtifactOnlyMana(color);
                amount += pool.getArtifactSpellOrAbilityOnlyMana(color);
            }
            if (creatureSpellOnlyContext) {
                amount += pool.getCreatureSpellOnlyMana(color);
            }
            if (instantSorceryOnlyColorlessContext) {
                amount += pool.getInstantSorceryOnlyColored(color);
            }
            if (restrictedRedContext && color == ManaColor.RED) {
                amount += pool.getRestrictedRed();
            }
            if (kickedOnlyGreenContext && color == ManaColor.GREEN) {
                amount += pool.getKickedOnlyGreen();
            }
            available.put(color, amount);
        }
        Map<ManaColor, Integer> before = new EnumMap<>(available);
        int[] extraGeneric = {0};
        assignHybrids(available, extraGeneric);
        for (ManaColor color : ManaColor.values()) {
            int remaining = before.get(color) - available.get(color);
            if (remaining <= 0) {
                continue;
            }
            int fromSubtypeCreature = Math.min(remaining,
                    pool.getSubtypeCreatureManaForColor(creatureCtx, color));
            if (fromSubtypeCreature > 0) {
                pool.removeSubtypeCreatureMana(creatureCtx, color, fromSubtypeCreature);
                remaining -= fromSubtypeCreature;
            }
            int fromSpellOnlySubtype = Math.min(remaining,
                    pool.getSubtypeSpellOnlyManaForColor(spellOnlyCtx, color));
            if (fromSpellOnlySubtype > 0) {
                pool.removeSubtypeSpellOnlyMana(spellOnlyCtx, color, fromSpellOnlySubtype);
                remaining -= fromSpellOnlySubtype;
            }
            int fromSubtypeSpellOrAbility = Math.min(remaining,
                    pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, color));
            if (fromSubtypeSpellOrAbility > 0) {
                pool.removeSubtypeSpellOrAbilityMana(soaCtx, color, fromSubtypeSpellOrAbility);
                remaining -= fromSubtypeSpellOrAbility;
            }
            int fromCreatureSourceSubtype = Math.min(remaining,
                    pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(creatureSourceSoaCtx, color));
            if (fromCreatureSourceSubtype > 0) {
                pool.removeSubtypeCreatureSourceSpellOrAbilityMana(
                        creatureSourceSoaCtx, color, fromCreatureSourceSubtype);
                remaining -= fromCreatureSourceSubtype;
            }
            int fromSubtypeOrPlaneswalker = Math.min(remaining,
                    pool.getSubtypeOrPlaneswalkerSpellManaForColor(subtypeOrPlaneswalkerCtx, color));
            if (fromSubtypeOrPlaneswalker > 0) {
                pool.removeSubtypeOrPlaneswalkerSpellMana(subtypeOrPlaneswalkerCtx, color, fromSubtypeOrPlaneswalker);
                remaining -= fromSubtypeOrPlaneswalker;
            }
            if (creatureSpellOnlyContext && remaining > 0) {
                int fromCreatureSpell = Math.min(remaining, pool.getCreatureSpellOnlyMana(color));
                pool.removeCreatureSpellOnlyMana(color, fromCreatureSpell);
                remaining -= fromCreatureSpell;
            }
            if (instantSorceryOnlyColorlessContext && remaining > 0) {
                int fromInstantSorcery = Math.min(remaining, pool.getInstantSorceryOnlyColored(color));
                pool.removeInstantSorceryOnlyColored(color, fromInstantSorcery);
                remaining -= fromInstantSorcery;
            }
            if (restrictedRedContext && color == ManaColor.RED && remaining > 0) {
                int fromRestrictedRed = Math.min(remaining, pool.getRestrictedRed());
                pool.removeRestrictedRed(fromRestrictedRed);
                remaining -= fromRestrictedRed;
            }
            if (kickedOnlyGreenContext && color == ManaColor.GREEN && remaining > 0) {
                int fromKickedGreen = Math.min(remaining, pool.getKickedOnlyGreen());
                pool.removeKickedOnlyGreen(fromKickedGreen);
                remaining -= fromKickedGreen;
            }
            if (artifactContext && remaining > 0) {
                int fromArtifact = Math.min(remaining, pool.getArtifactOnlyMana(color));
                pool.removeArtifactOnlyMana(color, fromArtifact);
                remaining -= fromArtifact;
                int fromGuidelight = Math.min(remaining, pool.getArtifactSpellOrAbilityOnlyMana(color));
                pool.removeArtifactSpellOrAbilityOnlyMana(color, fromGuidelight);
                remaining -= fromGuidelight;
            }
            for (int i = 0; i < remaining; i++) {
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
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext,
                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                    boolean instantSorceryOnlyColorlessContext, boolean powerstoneContext) {
        if (pool.isAllManaSpendableAsAnyColor()) {
            applyAllManaAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            applyWhiteAsRedForPayment(pool, p -> canPay(p, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            applyWhiteAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            applyWhiteAsAnyColorWithoutRestriction(pool);
        }
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
                } else if (artifactContext && pool.getArtifactOnlyMana(entry.getKey()) > 0) {
                    pool.removeArtifactOnlyMana(entry.getKey(), 1);
                } else if (artifactContext && pool.getArtifactSpellOrAbilityOnlyMana(entry.getKey()) > 0) {
                    pool.removeArtifactSpellOrAbilityOnlyMana(entry.getKey(), 1);
                } else if (instantSorceryOnlyColorlessContext && pool.getInstantSorceryOnlyColored(entry.getKey()) > 0) {
                    pool.removeInstantSorceryOnlyColored(entry.getKey(), 1);
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        // Pay hybrid symbols from the general pool, exactly as the context-free pay(ManaPool, int)
        // does. Without this a cost made only of hybrid pips ({R/G}{R/G}) would be free.
        int extraHybridGeneric = payHybrids(pool, artifactContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, null, null, false, Set.of(), Set.of());

        int remainingGeneric = genericCost + extraHybridGeneric + xValue * effectiveXMultiplier();

        // Spend more-restrictive mana first: Myr-only before artifact-only
        if (myrContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getMyrOnlyColorless());
            pool.removeMyrOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (powerstoneContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getPowerstoneOnlyColorless());
            pool.removePowerstoneOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        // Spend artifact-only colorless for generic costs
        if (artifactContext && remainingGeneric > 0) {
            for (ManaColor color : ManaColor.values()) {
                if (remainingGeneric <= 0) {
                    break;
                }
                int fromRestricted = Math.min(remainingGeneric, pool.getArtifactOnlyMana(color));
                pool.removeArtifactOnlyMana(color, fromRestricted);
                remainingGeneric -= fromRestricted;
            }
            int fromRestricted = Math.min(remainingGeneric, pool.getArtifactOnlyColorless());
            pool.removeArtifactOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
            for (ManaColor color : ManaColor.values()) {
                if (remainingGeneric <= 0) {
                    break;
                }
                int fromGuidelight = Math.min(remainingGeneric, pool.getArtifactSpellOrAbilityOnlyMana(color));
                pool.removeArtifactSpellOrAbilityOnlyMana(color, fromGuidelight);
                remainingGeneric -= fromGuidelight;
            }
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

    /** Pays with an independent generic-cost modifier under mana restrictions. */
    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                              boolean artifactContext, boolean myrContext,
                                              boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                              boolean instantSorceryOnlyColorlessContext) {
        payWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, false);
    }

    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                              boolean artifactContext, boolean myrContext,
                                              boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                              boolean instantSorceryOnlyColorlessContext,
                                              boolean powerstoneContext) {
        if (snowCost > 0) {
            pool.removeSnowMana(snowCost);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            applySnowManaAsAnyColor(pool);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            applyAllManaAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            applyWhiteAsRedForPayment(pool, p -> canPayWithAdditionalGenericCost(p, xValue,
                    additionalGenericCost, artifactContext, myrContext, restrictedRedContext,
                    kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, powerstoneContext));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            applyWhiteAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            applyWhiteAsAnyColorWithoutRestriction(pool);
        }
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                if (restrictedRedContext && entry.getKey() == ManaColor.RED && extraRed > 0) {
                    pool.removeRestrictedRed(1);
                    extraRed--;
                } else if (kickedOnlyGreenContext && entry.getKey() == ManaColor.GREEN && extraGreen > 0) {
                    pool.removeKickedOnlyGreen(1);
                    extraGreen--;
                } else if (artifactContext && pool.getArtifactSpellOrAbilityOnlyMana(entry.getKey()) > 0) {
                    pool.removeArtifactSpellOrAbilityOnlyMana(entry.getKey(), 1);
                } else if (instantSorceryOnlyColorlessContext && pool.getInstantSorceryOnlyColored(entry.getKey()) > 0) {
                    pool.removeInstantSorceryOnlyColored(entry.getKey(), 1);
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        int extraHybridGeneric = payHybrids(pool);
        int remainingGeneric = genericCost + extraHybridGeneric
                + xValue * effectiveXMultiplier() + additionalGenericCost;

        if (myrContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getMyrOnlyColorless());
            pool.removeMyrOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }
        if (powerstoneContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getPowerstoneOnlyColorless());
            pool.removePowerstoneOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }
        if (artifactContext && remainingGeneric > 0) {
            for (ManaColor color : ManaColor.values()) {
                if (remainingGeneric <= 0) {
                    break;
                }
                int fromGuidelight = Math.min(remainingGeneric, pool.getArtifactSpellOrAbilityOnlyMana(color));
                pool.removeArtifactSpellOrAbilityOnlyMana(color, fromGuidelight);
                remainingGeneric -= fromGuidelight;
            }
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
                if (remainingGeneric <= 0 || color == ManaColor.COLORLESS) {
                    break;
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

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, null);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext, Set.of());
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext, false,
                subtypeOrPlaneswalkerSpellContext);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, boolean manaValueAtLeastFourContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext,
                manaValueAtLeastFourContext, Set.of());
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, boolean manaValueAtLeastFourContext, Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext) {
        pay(pool, xValue, artifactContext, myrContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, subtypeCreatureContext, subtypeSpellOrAbilityContext,
                creatureSpellOnlyContext, artifactAbilityOnlyContext, legendarySpellOnlyContext,
                manaValueAtLeastFourContext, subtypeOrPlaneswalkerSpellContext, false);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext, boolean restrictedRedContext, boolean kickedOnlyGreenContext, boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext, Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext, boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext, boolean manaValueAtLeastFourContext, Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext, boolean powerstoneContext) {
        payWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, powerstoneContext);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext,
                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                    boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                    Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                    boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext,
                    boolean manaValueAtLeastFourContext,
                    Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                    Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext) {
        payWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext);
    }

    public void pay(ManaPool pool, int xValue, boolean artifactContext, boolean myrContext,
                    boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                    boolean instantSorceryOnlyColorlessContext, Set<CardSubtype> subtypeCreatureContext,
                    Set<CardSubtype> subtypeSpellOrAbilityContext, boolean creatureSpellOnlyContext,
                    boolean artifactAbilityOnlyContext, boolean legendarySpellOnlyContext,
                    boolean manaValueAtLeastFourContext,
                    Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                    Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                    boolean powerstoneContext) {
        payWithAdditionalGenericCost(pool, xValue, 0, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                powerstoneContext);
    }

    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                             boolean artifactContext, boolean myrContext,
                                             boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                             boolean instantSorceryOnlyColorlessContext,
                                             Set<CardSubtype> subtypeCreatureContext,
                                             Set<CardSubtype> subtypeSpellOrAbilityContext,
                                             boolean creatureSpellOnlyContext,
                                             boolean artifactAbilityOnlyContext,
                                             boolean legendarySpellOnlyContext,
                                             boolean manaValueAtLeastFourContext,
                                             Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext) {
        payWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, Set.of(), false);
    }

    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                              boolean artifactContext, boolean myrContext,
                                              boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                              boolean instantSorceryOnlyColorlessContext,
                                              Set<CardSubtype> subtypeCreatureContext,
                                              Set<CardSubtype> subtypeSpellOrAbilityContext,
                                              boolean creatureSpellOnlyContext,
                                              boolean artifactAbilityOnlyContext,
                                              boolean legendarySpellOnlyContext,
                                              boolean manaValueAtLeastFourContext,
                                              Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                              Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext) {
        payWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext, false);
    }

    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                             boolean artifactContext, boolean myrContext,
                                             boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                             boolean instantSorceryOnlyColorlessContext,
                                             Set<CardSubtype> subtypeCreatureContext,
                                             Set<CardSubtype> subtypeSpellOrAbilityContext,
                                             boolean creatureSpellOnlyContext,
                                             boolean artifactAbilityOnlyContext,
                                             boolean legendarySpellOnlyContext,
                                             boolean manaValueAtLeastFourContext,
                                             Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                             boolean powerstoneContext) {
        payWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, Set.of(), powerstoneContext);
    }

    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                             boolean artifactContext, boolean myrContext,
                                             boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                             boolean instantSorceryOnlyColorlessContext,
                                             Set<CardSubtype> subtypeCreatureContext,
                                             Set<CardSubtype> subtypeSpellOrAbilityContext,
                                             boolean creatureSpellOnlyContext,
                                             boolean artifactAbilityOnlyContext,
                                             boolean legendarySpellOnlyContext,
                                             boolean manaValueAtLeastFourContext,
                                             Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                             Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                                             boolean powerstoneContext) {
        payWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext, myrContext,
                restrictedRedContext, kickedOnlyGreenContext, instantSorceryOnlyColorlessContext,
                subtypeCreatureContext, subtypeSpellOrAbilityContext, creatureSpellOnlyContext,
                artifactAbilityOnlyContext, legendarySpellOnlyContext, manaValueAtLeastFourContext,
                subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                powerstoneContext, Set.of());
    }

    public void payWithAdditionalGenericCost(ManaPool pool, int xValue, int additionalGenericCost,
                                             boolean artifactContext, boolean myrContext,
                                             boolean restrictedRedContext, boolean kickedOnlyGreenContext,
                                             boolean instantSorceryOnlyColorlessContext,
                                             Set<CardSubtype> subtypeCreatureContext,
                                             Set<CardSubtype> subtypeSpellOrAbilityContext,
                                             boolean creatureSpellOnlyContext,
                                             boolean artifactAbilityOnlyContext,
                                             boolean legendarySpellOnlyContext,
                                             boolean manaValueAtLeastFourContext,
                                             Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerSpellContext,
                                             Set<CardSubtype> subtypeCreatureSourceSpellOrAbilityContext,
                                             boolean powerstoneContext,
                                             Set<CardSubtype> subtypeSpellOnlyContext) {
        if (snowCost > 0) {
            pool.removeSnowMana(snowCost);
        }
        if (pool.isSnowManaSpendableAsAnyColor()) {
            applySnowManaAsAnyColor(pool);
        }
        if (pool.isAllManaSpendableAsAnyColor()) {
            applyAllManaAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsRed() && requiresRed()) {
            applyWhiteAsRedForPayment(pool, p -> canPayWithAdditionalGenericCost(p, xValue,
                    additionalGenericCost, artifactContext, myrContext, restrictedRedContext,
                    kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext,
                    legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext, subtypeSpellOnlyContext));
        }
        if (pool.isBlueSpendableAsAnyColorForActivatedAbilities()) {
            applyBlueAsAnyColorForPayment(pool, p -> canPayWithAdditionalGenericCost(p, xValue,
                    additionalGenericCost, artifactContext, myrContext, restrictedRedContext,
                    kickedOnlyGreenContext, instantSorceryOnlyColorlessContext, subtypeCreatureContext,
                    subtypeSpellOrAbilityContext, creatureSpellOnlyContext, artifactAbilityOnlyContext,
                    legendarySpellOnlyContext, manaValueAtLeastFourContext,
                    subtypeOrPlaneswalkerSpellContext, subtypeCreatureSourceSpellOrAbilityContext,
                    powerstoneContext, subtypeSpellOnlyContext));
        }
        if (pool.isWhiteSpendableAsAnyColor()) {
            applyWhiteAsAnyColor(pool);
        }
        if (pool.isWhiteSpendableAsAnyColorWithoutRestriction()) {
            applyWhiteAsAnyColorWithoutRestriction(pool);
        }
        boolean hasCreatureCtx = subtypeCreatureContext != null && !subtypeCreatureContext.isEmpty();
        boolean hasSpellOrAbilityCtx = subtypeSpellOrAbilityContext != null && !subtypeSpellOrAbilityContext.isEmpty();
        boolean hasCreatureSourceSpellOrAbilityCtx = subtypeCreatureSourceSpellOrAbilityContext != null
                && !subtypeCreatureSourceSpellOrAbilityContext.isEmpty();
        boolean hasSubtypeOrPlaneswalkerSpellCtx = subtypeOrPlaneswalkerSpellContext != null
                && !subtypeOrPlaneswalkerSpellContext.isEmpty();
        boolean hasSpellOnlyCtx = subtypeSpellOnlyContext != null && !subtypeSpellOnlyContext.isEmpty();
        if (!hasCreatureCtx && !hasSpellOrAbilityCtx && !hasCreatureSourceSpellOrAbilityCtx
                && !hasSubtypeOrPlaneswalkerSpellCtx
                && !creatureSpellOnlyContext && !artifactAbilityOnlyContext && !legendarySpellOnlyContext
                && !manaValueAtLeastFourContext && !powerstoneContext && !hasSpellOnlyCtx) {
            payWithAdditionalGenericCost(pool, xValue, additionalGenericCost, artifactContext,
                    myrContext, restrictedRedContext, kickedOnlyGreenContext,
                    instantSorceryOnlyColorlessContext, powerstoneContext);
            return;
        }
        Set<CardSubtype> creatureCtx = hasCreatureCtx ? subtypeCreatureContext : Set.of();
        Set<CardSubtype> soaCtx = hasSpellOrAbilityCtx ? subtypeSpellOrAbilityContext : Set.of();
        Set<CardSubtype> creatureSourceSoaCtx = hasCreatureSourceSpellOrAbilityCtx
                ? subtypeCreatureSourceSpellOrAbilityContext : Set.of();
        Set<ManaRestriction.SubtypeOrPlaneswalkerSpells> subtypeOrPlaneswalkerCtx =
                hasSubtypeOrPlaneswalkerSpellCtx ? subtypeOrPlaneswalkerSpellContext : Set.of();
        Set<CardSubtype> spellOnlyCtx = hasSpellOnlyCtx ? subtypeSpellOnlyContext : Set.of();
        int extraRed = restrictedRedContext ? pool.getRestrictedRed() : 0;
        int extraGreen = kickedOnlyGreenContext ? pool.getKickedOnlyGreen() : 0;

        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                // Prefer spending the most restricted mana first.
                if (manaValueAtLeastFourContext && pool.getManaValueAtLeastFourOnlyMana(entry.getKey()) > 0) {
                    pool.removeManaValueAtLeastFourOnlyMana(entry.getKey(), 1);
                } else if (pool.getSubtypeCreatureManaForColor(creatureCtx, entry.getKey()) > 0) {
                    pool.removeSubtypeCreatureMana(creatureCtx, entry.getKey(), 1);
                } else if (pool.getSubtypeSpellOnlyManaForColor(spellOnlyCtx, entry.getKey()) > 0) {
                    pool.removeSubtypeSpellOnlyMana(spellOnlyCtx, entry.getKey(), 1);
                } else if (pool.getSubtypeSpellOrAbilityManaForColor(soaCtx, entry.getKey()) > 0) {
                    pool.removeSubtypeSpellOrAbilityMana(soaCtx, entry.getKey(), 1);
                } else if (pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(
                        creatureSourceSoaCtx, entry.getKey()) > 0) {
                    pool.removeSubtypeCreatureSourceSpellOrAbilityMana(
                            creatureSourceSoaCtx, entry.getKey(), 1);
                } else if (pool.getSubtypeOrPlaneswalkerSpellManaForColor(subtypeOrPlaneswalkerCtx, entry.getKey()) > 0) {
                    pool.removeSubtypeOrPlaneswalkerSpellMana(subtypeOrPlaneswalkerCtx, entry.getKey(), 1);
                } else if (creatureSpellOnlyContext && pool.getCreatureSpellOnlyMana(entry.getKey()) > 0) {
                    pool.removeCreatureSpellOnlyMana(entry.getKey(), 1);
                } else if (restrictedRedContext && entry.getKey() == ManaColor.RED && extraRed > 0) {
                    pool.removeRestrictedRed(1);
                    extraRed--;
                } else if (kickedOnlyGreenContext && entry.getKey() == ManaColor.GREEN && extraGreen > 0) {
                    pool.removeKickedOnlyGreen(1);
                    extraGreen--;
                } else if (artifactContext && pool.getArtifactOnlyMana(entry.getKey()) > 0) {
                    pool.removeArtifactOnlyMana(entry.getKey(), 1);
                } else if (artifactContext && pool.getArtifactSpellOrAbilityOnlyMana(entry.getKey()) > 0) {
                    pool.removeArtifactSpellOrAbilityOnlyMana(entry.getKey(), 1);
                } else if (instantSorceryOnlyColorlessContext && pool.getInstantSorceryOnlyColored(entry.getKey()) > 0) {
                    pool.removeInstantSorceryOnlyColored(entry.getKey(), 1);
                } else {
                    pool.remove(entry.getKey());
                }
            }
        }

        // Pay hybrid symbols from the general pool, exactly as the context-free pay(ManaPool, int)
        // does. Without this a cost made only of hybrid pips ({R/G}{R/G}) would be free.
        int extraHybridGeneric = payHybrids(pool, artifactContext, restrictedRedContext, kickedOnlyGreenContext,
                instantSorceryOnlyColorlessContext, creatureCtx, soaCtx, creatureSpellOnlyContext,
                subtypeOrPlaneswalkerCtx, creatureSourceSoaCtx, spellOnlyCtx);

        int remainingGeneric = genericCost + extraHybridGeneric
                + xValue * effectiveXMultiplier() + additionalGenericCost;

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

        // Spend subtype spell-only mana for generic costs (also fully restricted)
        if (remainingGeneric > 0) {
            int subtypeTotal = pool.getSubtypeSpellOnlyManaTotal(spellOnlyCtx);
            int fromSubtype = Math.min(remainingGeneric, subtypeTotal);
            if (fromSubtype > 0) {
                int toRemove = fromSubtype;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getSubtypeSpellOnlyManaForColor(spellOnlyCtx, color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeSubtypeSpellOnlyMana(spellOnlyCtx, color, removeNow);
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

        if (remainingGeneric > 0) {
            int subtypeTotal = pool.getSubtypeCreatureSourceSpellOrAbilityManaTotal(creatureSourceSoaCtx);
            int fromSubtype = Math.min(remainingGeneric, subtypeTotal);
            if (fromSubtype > 0) {
                int toRemove = fromSubtype;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getSubtypeCreatureSourceSpellOrAbilityManaForColor(
                            creatureSourceSoaCtx, color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeSubtypeCreatureSourceSpellOrAbilityMana(
                                creatureSourceSoaCtx, color, removeNow);
                        toRemove -= removeNow;
                    }
                }
                remainingGeneric -= fromSubtype;
            }
        }

        // Spend subtype-or-planeswalker spell mana for generic costs before less restricted buckets.
        if (remainingGeneric > 0) {
            int subtypeTotal = pool.getSubtypeOrPlaneswalkerSpellManaTotal(subtypeOrPlaneswalkerCtx);
            int fromSubtype = Math.min(remainingGeneric, subtypeTotal);
            if (fromSubtype > 0) {
                int toRemove = fromSubtype;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getSubtypeOrPlaneswalkerSpellManaForColor(subtypeOrPlaneswalkerCtx, color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeSubtypeOrPlaneswalkerSpellMana(subtypeOrPlaneswalkerCtx, color, removeNow);
                        toRemove -= removeNow;
                    }
                }
                remainingGeneric -= fromSubtype;
            }
        }

        // Spend creature-spell-only mana for generic costs (fully restricted to this spell)
        if (creatureSpellOnlyContext && remainingGeneric > 0) {
            int creatureSpellTotal = pool.getCreatureSpellOnlyManaTotal();
            int fromCreatureSpell = Math.min(remainingGeneric, creatureSpellTotal);
            if (fromCreatureSpell > 0) {
                int toRemove = fromCreatureSpell;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int avail = pool.getCreatureSpellOnlyMana(color);
                    int removeNow = Math.min(toRemove, avail);
                    if (removeNow > 0) {
                        pool.removeCreatureSpellOnlyMana(color, removeNow);
                        toRemove -= removeNow;
                    }
                }
                remainingGeneric -= fromCreatureSpell;
            }
        }

        // Spend mana-value-restricted mana for generic costs.
        if (manaValueAtLeastFourContext && remainingGeneric > 0) {
            int restrictedTotal = pool.getManaValueAtLeastFourOnlyManaTotal();
            int fromRestricted = Math.min(remainingGeneric, restrictedTotal);
            if (fromRestricted > 0) {
                int toRemove = fromRestricted;
                for (ManaColor color : ManaColor.values()) {
                    if (toRemove <= 0) break;
                    int available = pool.getManaValueAtLeastFourOnlyMana(color);
                    int removeNow = Math.min(toRemove, available);
                    if (removeNow > 0) {
                        pool.removeManaValueAtLeastFourOnlyMana(color, removeNow);
                        toRemove -= removeNow;
                    }
                }
                remainingGeneric -= fromRestricted;
            }
        }

        // Spend legendary-spell-only mana for generic costs (fully restricted to this spell)
        if (legendarySpellOnlyContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getLegendarySpellOnlyColorless());
            pool.removeLegendarySpellOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        // Spend more-restrictive mana first: Myr-only, then artifact-ability-only, then artifact-only
        if (myrContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getMyrOnlyColorless());
            pool.removeMyrOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (artifactAbilityOnlyContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getArtifactAbilityOnlyColorless());
            pool.removeArtifactAbilityOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (powerstoneContext && remainingGeneric > 0) {
            int fromRestricted = Math.min(remainingGeneric, pool.getPowerstoneOnlyColorless());
            pool.removePowerstoneOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
        }

        if (artifactContext && remainingGeneric > 0) {
            for (ManaColor color : ManaColor.values()) {
                if (remainingGeneric <= 0) {
                    break;
                }
                int fromRestricted = Math.min(remainingGeneric, pool.getArtifactOnlyMana(color));
                pool.removeArtifactOnlyMana(color, fromRestricted);
                remainingGeneric -= fromRestricted;
            }
            int fromRestricted = Math.min(remainingGeneric, pool.getArtifactOnlyColorless());
            pool.removeArtifactOnlyColorless(fromRestricted);
            remainingGeneric -= fromRestricted;
            for (ManaColor color : ManaColor.values()) {
                if (remainingGeneric <= 0) {
                    break;
                }
                int fromGuidelight = Math.min(remainingGeneric, pool.getArtifactSpellOrAbilityOnlyMana(color));
                pool.removeArtifactSpellOrAbilityOnlyMana(color, fromGuidelight);
                remainingGeneric -= fromGuidelight;
            }
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

    /**
     * Pays an X cost where each point of X must come from {@code xColorRestriction}.
     *
     * @return per-color counts of mana removed specifically for X (not for the rest of the cost)
     */
    public EnumMap<ManaColor, Integer> pay(ManaPool pool, int xValue, ManaColor xColorRestriction, int additionalGenericCost) {
        return pay(pool, xValue, java.util.EnumSet.of(xColorRestriction), additionalGenericCost);
    }

    /**
     * Pays an X cost where each point of X must come from one of {@code xColorRestrictions}.
     * Prefers earlier colors in {@link ManaColor} enum order when multiple are available, which
     * for Soul Burn (BLACK before RED) maximizes black spent on X — and thus life gained.
     *
     * @return per-color counts of mana removed specifically for X (not for the rest of the cost)
     */
    public EnumMap<ManaColor, Integer> pay(ManaPool pool, int xValue, Set<ManaColor> xColorRestrictions,
                                           int additionalGenericCost) {
        if (pool.isAllManaSpendableAsAnyColor()) {
            applyAllManaAsAnyColor(pool, xColorRestrictions, xValue * xSymbolCount);
        }
        for (Map.Entry<ManaColor, Integer> entry : coloredCosts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.remove(entry.getKey());
            }
        }

        EnumMap<ManaColor, Integer> spentOnX = new EnumMap<>(ManaColor.class);
        int totalX = xValue * xSymbolCount;
        for (int i = 0; i < totalX; i++) {
            ManaColor chosen = null;
            for (ManaColor color : ManaColor.values()) {
                if (xColorRestrictions.contains(color) && pool.get(color) > 0) {
                    chosen = color;
                    break;
                }
            }
            if (chosen == null) {
                throw new IllegalStateException("Not enough restricted mana to pay X");
            }
            pool.remove(chosen);
            spentOnX.merge(chosen, 1, Integer::sum);
        }

        payGenericPreferColorless(pool, genericCost + additionalGenericCost);
        return spentOnX;
    }

    /**
     * Check if the cost can be paid with convoke contributions.
     * Each convoke contribution pays for one mana: colored if it matches an unpaid colored or
     * hybrid cost, otherwise reduces generic cost. Null entries represent colorless creatures
     * (generic only).
     */
    public boolean canPayWithConvoke(ManaPool pool, int additionalGenericCost, List<ManaColor> convokeContributions) {
        if (canTreatConvokeAsRegularMana(pool)
                && hybridCosts.stream().allMatch(hybrid -> hybrid.genericAlternative() < 0)) {
            ManaPool augmentedPool = pool instanceof VirtualManaPool virtualPool
                    ? new VirtualManaPool(virtualPool)
                    : new ManaPool(pool);
            int genericOnlyContributions = 0;
            for (ManaColor contribution : convokeContributions != null
                    ? convokeContributions : List.<ManaColor>of()) {
                if (contribution == null || contribution == ManaColor.COLORLESS) {
                    genericOnlyContributions++;
                } else {
                    augmentedPool.add(contribution);
                }
            }
            return remainingConvokeCost(additionalGenericCost, genericOnlyContributions)
                    .canPay(augmentedPool);
        }
        return findConvokePaymentPlan(pool, additionalGenericCost, convokeContributions) != null;
    }

    /**
     * Pay the cost using convoke contributions and the mana pool.
     * Each convoke contribution pays for one mana: colored if it matches an unpaid colored or
     * hybrid cost, otherwise reduces generic cost.
     */
    public void payWithConvoke(ManaPool pool, int additionalGenericCost, List<ManaColor> convokeContributions) {
        ManaCost remainingCost = findConvokePaymentPlan(pool, additionalGenericCost, convokeContributions);
        if (remainingCost == null) {
            throw new IllegalStateException("Mana cost cannot be paid with convoke");
        }
        remainingCost.payWithAdditionalGenericCost(pool, 0, 0);
    }

    private ManaCost findConvokePaymentPlan(ManaPool pool, int additionalGenericCost,
                                             List<ManaColor> convokeContributions) {
        Map<ManaColor, Integer> remainingColored = new EnumMap<>(coloredCosts);
        List<HybridSymbol> remainingHybrids = new ArrayList<>(hybridCosts);
        int remainingGeneric = Math.max(0, genericCost + additionalGenericCost);
        List<ManaColor> contributions = convokeContributions != null
                ? convokeContributions : List.of();
        Set<ConvokePaymentState> failedStates = new HashSet<>();
        return findConvokePaymentPlan(pool, contributions, 0, remainingGeneric,
                remainingColored, remainingHybrids, failedStates);
    }

    private ManaCost remainingConvokeCost(int additionalGenericCost, int genericOnlyContributions) {
        EnumMap<ManaColor, Integer> noPhyrexianCosts = new EnumMap<>(ManaColor.class);
        return new ManaCost(
                Math.max(0, genericCost + additionalGenericCost - genericOnlyContributions), coloredCosts,
                noPhyrexianCosts, hybridCosts, 0, 0, cumulativeUpkeepPayment);
    }

    private static boolean canTreatConvokeAsRegularMana(ManaPool pool) {
        return pool != null
                && (pool.getClass() == ManaPool.class || pool instanceof VirtualManaPool)
                && pool.getTotalAllMana() == pool.getTotal()
                && !pool.isWhiteSpendableAsRed()
                && !pool.isWhiteSpendableAsAnyColor()
                && !pool.isWhiteSpendableAsAnyColorWithoutRestriction()
                && !pool.isAllManaSpendableAsAnyColor()
                && !pool.isBlueSpendableAsAnyColorForActivatedAbilities();
    }

    private ManaCost findConvokePaymentPlan(ManaPool pool, List<ManaColor> contributions, int index,
                                             int remainingGeneric,
                                             Map<ManaColor, Integer> remainingColored,
                                             List<HybridSymbol> remainingHybrids,
                                             Set<ConvokePaymentState> failedStates) {
        List<HybridSymbol> canonicalHybrids = canonicalHybridCosts(remainingHybrids);
        EnumMap<ManaColor, Integer> noPhyrexianCosts = new EnumMap<>(ManaColor.class);
        ManaCost remainingCost = new ManaCost(
                remainingGeneric, remainingColored, noPhyrexianCosts, canonicalHybrids, 0,
                0, cumulativeUpkeepPayment);
        if (remainingCost.canPay(pool)) {
            return remainingCost;
        }
        if (index == contributions.size()) {
            failedStates.add(new ConvokePaymentState(
                    index, remainingGeneric, remainingColored, canonicalHybrids));
            return null;
        }
        ConvokePaymentState state = new ConvokePaymentState(
                index, remainingGeneric, remainingColored, canonicalHybrids);
        if (failedStates.contains(state)) {
            return null;
        }

        ManaColor contribution = contributions.get(index);
        ManaCost plan = findConvokePaymentPlan(pool, contributions, index + 1,
                remainingGeneric, remainingColored, canonicalHybrids, failedStates);
        if (plan != null) {
            return plan;
        }
        if (contribution == null || contribution == ManaColor.COLORLESS) {
            if (remainingGeneric == 0) {
                for (int i = 0; i < canonicalHybrids.size(); i++) {
                    HybridSymbol hybrid = canonicalHybrids.get(i);
                    if (hybrid.genericAlternative() <= 0) {
                        continue;
                    }
                    List<HybridSymbol> nextHybrids = new ArrayList<>(canonicalHybrids);
                    nextHybrids.set(i, new HybridSymbol(
                            hybrid.colors(), hybrid.genericAlternative() - 1, hybrid.phyrexianAlternative()));
                    plan = findConvokePaymentPlan(pool, contributions, index + 1,
                            remainingGeneric, remainingColored, nextHybrids, failedStates);
                    if (plan != null) {
                        return plan;
                    }
                }
                return null;
            }
            plan = findConvokePaymentPlan(pool, contributions, index + 1,
                    remainingGeneric - 1, remainingColored, canonicalHybrids, failedStates);
            if (plan != null) {
                return plan;
            }
            for (int i = 0; i < canonicalHybrids.size(); i++) {
                HybridSymbol hybrid = canonicalHybrids.get(i);
                if (hybrid.genericAlternative() <= 0) {
                    continue;
                }
                List<HybridSymbol> nextHybrids = new ArrayList<>(canonicalHybrids);
                nextHybrids.set(i, new HybridSymbol(
                        hybrid.colors(), hybrid.genericAlternative() - 1, hybrid.phyrexianAlternative()));
                plan = findConvokePaymentPlan(pool, contributions, index + 1,
                        remainingGeneric, remainingColored, nextHybrids, failedStates);
                if (plan != null) {
                    return plan;
                }
            }
            failedStates.add(state);
            return null;
        }

        int coloredRemaining = remainingColored.getOrDefault(contribution, 0);
        if (coloredRemaining > 0) {
            Map<ManaColor, Integer> nextColored = new EnumMap<>(remainingColored);
            nextColored.put(contribution, coloredRemaining - 1);
            plan = findConvokePaymentPlan(pool, contributions, index + 1,
                    remainingGeneric, nextColored, canonicalHybrids, failedStates);
            if (plan != null) {
                return plan;
            }
        }

        for (int i = 0; i < canonicalHybrids.size(); i++) {
            HybridSymbol hybrid = canonicalHybrids.get(i);
            if (!hybrid.colors().contains(contribution)) {
                continue;
            }
            List<HybridSymbol> nextHybrids = new ArrayList<>(canonicalHybrids);
            nextHybrids.remove(i);
            plan = findConvokePaymentPlan(pool, contributions, index + 1,
                    remainingGeneric, remainingColored, nextHybrids, failedStates);
            if (plan != null) {
                return plan;
            }
        }

        if (remainingGeneric > 0) {
            plan = findConvokePaymentPlan(pool, contributions, index + 1,
                    remainingGeneric - 1, remainingColored, canonicalHybrids, failedStates);
            if (plan != null) {
                return plan;
            }
        }

        for (int i = 0; i < canonicalHybrids.size(); i++) {
            HybridSymbol hybrid = canonicalHybrids.get(i);
            if (hybrid.genericAlternative() <= 0) {
                continue;
            }
            List<HybridSymbol> nextHybrids = new ArrayList<>(canonicalHybrids);
            nextHybrids.set(i, new HybridSymbol(
                    hybrid.colors(), hybrid.genericAlternative() - 1, hybrid.phyrexianAlternative()));
            plan = findConvokePaymentPlan(pool, contributions, index + 1,
                    remainingGeneric, remainingColored, nextHybrids, failedStates);
            if (plan != null) {
                return plan;
            }
        }
        failedStates.add(state);
        return null;
    }

    private static List<HybridSymbol> canonicalHybridCosts(List<HybridSymbol> hybrids) {
        List<HybridSymbol> canonical = new ArrayList<>(hybrids);
        canonical.sort(Comparator.comparingInt(ManaCost::hybridColorMask)
                .thenComparingInt(HybridSymbol::genericAlternative));
        return canonical;
    }

    private static int hybridColorMask(HybridSymbol hybrid) {
        int mask = 0;
        for (ManaColor color : hybrid.colors()) {
            mask |= 1 << color.ordinal();
        }
        return mask;
    }

    /**
     * Checks whether the pool has enough total mana to pay the mana value,
     * ignoring color requirements (mana of any type can be spent).
     */
    public boolean canPayAsGeneric(ManaPool pool) {
        return pool.getTotal() >= getManaValue();
    }

    /**
     * {@link #canPayAsGeneric(ManaPool)} including the chosen X (times the {X} multiplier) and an
     * additional generic cost modifier (positive taxes, negative reductions).
     */
    public boolean canPayAsGeneric(ManaPool pool, int xValue, int additionalGenericCost) {
        return pool.getTotal() >= getManaValue() + xValue * effectiveXMultiplier() + additionalGenericCost;
    }

    /**
     * Pays the full mana value using any mana from the pool, ignoring color requirements
     * (mana of any type can be spent to cast the spell).
     */
    public void payAsGeneric(ManaPool pool) {
        payGenericPreferColorless(pool, getManaValue());
    }

    /**
     * {@link #payAsGeneric(ManaPool)} including the chosen X (times the {X} multiplier) and an
     * additional generic cost modifier (positive taxes, negative reductions).
     */
    public void payAsGeneric(ManaPool pool, int xValue, int additionalGenericCost) {
        payGenericPreferColorless(pool, getManaValue() + xValue * effectiveXMultiplier() + additionalGenericCost);
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
