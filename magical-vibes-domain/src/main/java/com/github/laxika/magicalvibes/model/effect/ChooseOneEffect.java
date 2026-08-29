package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Modal spell effect: prompts the controller to choose one of the given options,
 * then resolves only the chosen option's effect.
 * <p>
 * Used by cards like Slagstorm: "Choose one — Slagstorm deals 3 damage to each creature.
 * — Slagstorm deals 3 damage to each player."
 * <p>
 * {@code choicesRequired} is the minimum number of modes that must be chosen;
 * {@code choicesMax} is the maximum (inclusive). Classic "choose one" is {@code (1, 1)};
 * "choose two" is {@code (2, 2)}; "choose one or more" is {@code (1, options.size())}.
 */
public record ChooseOneEffect(List<ChooseOneOption> options, boolean optional, int choicesRequired, int choicesMax,
                              boolean allModesWhenOptionalCostPaid, List<Integer> modeCosts, int modeBudget)
        implements CardEffect {

    public ChooseOneEffect {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty");
        }
        if (modeBudget < 0) {
            throw new IllegalArgumentException("modeBudget must be >= 0");
        }
        if (modeBudget == 0 && choicesRequired < 1) {
            throw new IllegalArgumentException("choicesRequired must be >= 1");
        }
        if (choicesMax < choicesRequired) {
            throw new IllegalArgumentException("choicesMax must be >= choicesRequired");
        }
        if (modeBudget == 0 && !modeCosts.isEmpty()) {
            throw new IllegalArgumentException("modeCosts require a positive modeBudget");
        }
        if (modeBudget > 0) {
            if (modeCosts.size() != options.size()) {
                throw new IllegalArgumentException("modeCosts must contain one entry per mode");
            }
            if (choicesRequired != 0 || choicesMax != modeBudget) {
                throw new IllegalArgumentException("Budgeted modes must use a zero-to-budget choice range");
            }
            if (modeCosts.stream().anyMatch(cost -> cost == null || cost < 1)) {
                throw new IllegalArgumentException("mode costs must be positive");
            }
        }
    }

    public ChooseOneEffect(List<ChooseOneOption> options) {
        this(options, false, 1, 1, false, List.of(), 0);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, boolean optional) {
        this(options, optional, 1, 1, false, List.of(), 0);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, int choicesRequired) {
        this(options, false, choicesRequired, choicesRequired, false, List.of(), 0);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, boolean optional, int choicesRequired, int choicesMax) {
        this(options, optional, choicesRequired, choicesMax, false, List.of(), 0);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, boolean optional, int choicesRequired, int choicesMax,
                           boolean allModesWhenOptionalCostPaid) {
        this(options, optional, choicesRequired, choicesMax, allModesWhenOptionalCostPaid, List.of(), 0);
    }

    /** "Choose one or more —" modal: at least one mode, up to every mode. */
    public static ChooseOneEffect oneOrMore(List<ChooseOneOption> options) {
        return new ChooseOneEffect(options, false, 1, options.size(), false);
    }

    /**
     * Modal selection where each mode has a cost and the controller may spend up to a fixed budget.
     * A mode may be selected repeatedly.
     */
    public static ChooseOneEffect budgetedModes(List<ChooseOneOption> options, List<Integer> modeCosts,
                                                 int modeBudget) {
        return new ChooseOneEffect(options, false, 0, modeBudget, false, List.copyOf(modeCosts), modeBudget);
    }

    /**
     * Encodes a modal selection for casting. Exact choose-one ({@code choicesRequired == choicesMax == 1})
     * uses a 0-based mode index; any multi-mode or variable-count spell uses a negative bitmask
     * ({@code -(1 << mode0 | 1 << mode1 | ...)}), including selecting a single mode of a
     * "choose one or more" spell.
     */
    public static int encodeModeSelection(int choicesRequired, int... modeIndices) {
        return encodeModeSelection(choicesRequired, choicesRequired, modeIndices);
    }

    /** Variable-count / ranged modal encoding ({@code choicesMin}..{@code choicesMax}). */
    public static int encodeModeSelection(int choicesMin, int choicesMax, int[] modeIndices) {
        if (choicesMin == 1 && choicesMax == 1) {
            if (modeIndices.length != 1) {
                throw new IllegalArgumentException("Choose-one requires exactly one mode index");
            }
            return modeIndices[0];
        }
        if (modeIndices.length < choicesMin || modeIndices.length > choicesMax) {
            throw new IllegalArgumentException(
                    "Expected between " + choicesMin + " and " + choicesMax + " mode indices");
        }
        int mask = 0;
        for (int modeIndex : modeIndices) {
            mask |= (1 << modeIndex);
        }
        return -mask;
    }

    /** Encodes a selection for a budgeted modal, including repeated mode selections. */
    public static int encodeModeSelection(ChooseOneEffect modal, int... modeIndices) {
        if (!modal.isBudgeted()) {
            return encodeModeSelection(modal.choicesRequired(), modal.choicesMax(), modeIndices);
        }
        return encodeBudgetedModeSelection(modal.modeBudget(), modal.modeCosts(), modeIndices);
    }

    /** Encodes a repeated-mode selection when only the mode costs and budget are available. */
    public static int encodeBudgetedModeSelection(int modeBudget, List<Integer> modeCosts,
                                                   int... modeIndices) {
        if (modeBudget < 1 || modeCosts.isEmpty()) {
            throw new IllegalArgumentException("Budgeted modal requires a positive budget and modes");
        }
        long encoded = 0;
        long base = (long) modeBudget + 1;
        int totalCost = 0;
        int[] counts = new int[modeCosts.size()];
        for (int modeIndex : modeIndices) {
            if (modeIndex < 0 || modeIndex >= modeCosts.size()) {
                throw new IllegalArgumentException("Invalid mode index: " + modeIndex);
            }
            counts[modeIndex]++;
            totalCost += modeCosts.get(modeIndex);
            if (totalCost > modeBudget) {
                throw new IllegalArgumentException("Mode selections exceed the modal budget");
            }
        }
        long place = 1;
        for (int count : counts) {
            encoded += place * count;
            place *= base;
        }
        if (encoded > Integer.MAX_VALUE - 1) {
            throw new IllegalArgumentException("Modal selection encoding exceeds integer range");
        }
        return (int) (-encoded - 1);
    }

    /** Returns the chosen mode indices in card-text order. */
    public List<Integer> decodeModeIndices(int xValue) {
        if (isBudgeted()) {
            if (xValue >= 0) {
                throw new IllegalStateException("Invalid budgeted mode encoding: " + xValue);
            }
            long encoded = -(long) xValue - 1;
            long base = (long) modeBudget + 1;
            List<Integer> chosen = new java.util.ArrayList<>();
            int totalCost = 0;
            for (int i = 0; i < options.size(); i++) {
                int count = (int) (encoded % base);
                encoded /= base;
                for (int j = 0; j < count; j++) {
                    chosen.add(i);
                    totalCost += modeCosts.get(i);
                }
            }
            if (encoded != 0 || totalCost > modeBudget) {
                throw new IllegalStateException("Invalid budgeted modal selection");
            }
            return chosen;
        }
        if (choicesRequired == 1 && choicesMax == 1) {
            if (xValue < 0 || xValue >= options.size()) {
                throw new IllegalStateException("Invalid mode index: " + xValue);
            }
            return List.of(xValue);
        }
        if (xValue >= 0) {
            throw new IllegalStateException("Invalid mode bitmask: " + xValue);
        }
        int mask = -xValue;
        List<Integer> chosen = new java.util.ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            if ((mask & (1 << i)) != 0) {
                chosen.add(i);
            }
        }
        if (chosen.size() < choicesRequired || chosen.size() > choicesMax) {
            throw new IllegalStateException(
                    "Expected between " + choicesRequired + " and " + choicesMax + " modes, got " + chosen.size());
        }
        return chosen;
    }

    /** True when this modal allows a variable number of modes (e.g. "choose one or more"). */
    public boolean variableModeCount() {
        return choicesMax > choicesRequired;
    }

    public boolean isBudgeted() {
        return modeBudget > 0;
    }

    /**
     * A single selectable mode. A mode may resolve one or more effects (e.g. "Surveil 2, then draw
     * a card" or "each opponent loses 3 life and you gain 3 life"); when chosen, all of the mode's
     * effects are spliced into the spell's resolution in order. The optional {@code targetFilter}
     * overrides the spell's cast-time target filter for this mode.
     * <p>
     * A mode whose effects each target a distinct object (e.g. Choreographed Sparks' "both" mode:
     * copy one instant/sorcery spell AND one creature spell) supplies {@code targetFilters} — one
     * per declared target — instead. When present, the modal cast declares one {@code target()} slot
     * per filter and maps each of the mode's effects (in order) to its own target index.
     * <p>
     * {@code minTargets}/{@code maxTargets}/{@code xScaledTargets} control the target group
     * declared from {@code targetFilter} at unwrap time. Defaults are a single required target
     * ({@code 1, 1, false}). Pass {@code xScaledTargets=true} with {@code minTargets=0} for
     * "up to X target …" modes (Profane Command's fear mode) — the paid X bounds the count via
     * {@link com.github.laxika.magicalvibes.model.Card#targetX}.
     * <p>
     * {@code manaCost} is a per-mode total mana cost that replaces the card's printed cost when this
     * mode is chosen. It is {@code null} for ordinary modals (every mode costs the same) and is used
     * by split cards with fuse (CR 709.3 / CR 702.102c): each half is a mode paying that half's cost
     * and the fuse mode pays both halves combined.
     */
    public record ChooseOneOption(String label, List<CardEffect> effects, TargetFilter targetFilter,
                                  List<TargetFilter> targetFilters, int minTargets, int maxTargets,
                                  boolean xScaledTargets, String manaCost) {
        public ChooseOneOption {
            if (minTargets < 0) {
                throw new IllegalArgumentException("minTargets must be >= 0");
            }
            if (maxTargets < minTargets) {
                throw new IllegalArgumentException("maxTargets must be >= minTargets");
            }
        }

        public ChooseOneOption(String label, CardEffect effect) {
            this(label, List.of(effect), null, null, 1, 1, false, null);
        }

        public ChooseOneOption(String label, CardEffect effect, TargetFilter targetFilter) {
            this(label, List.of(effect), targetFilter, null, 1, 1, false, null);
        }

        public ChooseOneOption(String label, List<CardEffect> effects) {
            this(label, effects, null, null, 1, 1, false, null);
        }

        public ChooseOneOption(String label, List<CardEffect> effects, TargetFilter targetFilter) {
            this(label, effects, targetFilter, null, 1, 1, false, null);
        }

        /** Multi-target mode: one target filter per effect, mapped positionally. */
        public ChooseOneOption(String label, List<CardEffect> effects, List<TargetFilter> targetFilters) {
            this(label, effects, null, targetFilters, 1, 1, false, null);
        }

        /**
         * Mode whose target count scales with the spell's paid X ("up to X target creatures …").
         * {@code cap} is only a sanity ceiling; the effective max is {@code min(X, cap)}.
         */
        public static ChooseOneOption upToXTargets(String label, CardEffect effect, TargetFilter filter, int cap) {
            return new ChooseOneOption(label, List.of(effect), filter, null, 0, cap, true, null);
        }

        /** A mode whose target count is exactly the spell's paid X value. */
        public static ChooseOneOption exactlyXTargets(String label, CardEffect effect,
                                                       TargetFilter filter, int cap) {
            return new ChooseOneOption(label, List.of(effect), filter, null, cap, cap, true, null);
        }

        /** This mode with its own total mana cost (split-card half / fuse mode). */
        public ChooseOneOption withManaCost(String manaCost) {
            return new ChooseOneOption(label, effects, targetFilter, targetFilters,
                    minTargets, maxTargets, xScaledTargets, manaCost);
        }

        /** Backward-compatible accessor for single-effect modes (returns the first effect). */
        public CardEffect effect() {
            return effects.getFirst();
        }
    }
}
