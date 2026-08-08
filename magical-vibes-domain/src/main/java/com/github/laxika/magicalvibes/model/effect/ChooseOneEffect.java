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
public record ChooseOneEffect(List<ChooseOneOption> options, boolean optional, int choicesRequired, int choicesMax)
        implements CardEffect {

    public ChooseOneEffect {
        if (choicesRequired < 1) {
            throw new IllegalArgumentException("choicesRequired must be >= 1");
        }
        if (choicesMax < choicesRequired) {
            throw new IllegalArgumentException("choicesMax must be >= choicesRequired");
        }
    }

    public ChooseOneEffect(List<ChooseOneOption> options) {
        this(options, false, 1, 1);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, boolean optional) {
        this(options, optional, 1, 1);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, int choicesRequired) {
        this(options, false, choicesRequired, choicesRequired);
    }

    /** "Choose one or more —" modal: at least one mode, up to every mode. */
    public static ChooseOneEffect oneOrMore(List<ChooseOneOption> options) {
        return new ChooseOneEffect(options, false, 1, options.size());
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

    /** Returns the chosen mode indices in card-text order. */
    public List<Integer> decodeModeIndices(int xValue) {
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
