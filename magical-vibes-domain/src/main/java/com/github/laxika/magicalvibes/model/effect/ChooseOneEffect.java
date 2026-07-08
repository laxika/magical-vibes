package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Modal spell effect: prompts the controller to choose one of the given options,
 * then resolves only the chosen option's effect.
 * <p>
 * Used by cards like Slagstorm: "Choose one — Slagstorm deals 3 damage to each creature.
 * — Slagstorm deals 3 damage to each player."
 */
public record ChooseOneEffect(List<ChooseOneOption> options, boolean optional, int choicesRequired) implements CardEffect {

    public ChooseOneEffect(List<ChooseOneOption> options) {
        this(options, false, 1);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, boolean optional) {
        this(options, optional, 1);
    }

    public ChooseOneEffect(List<ChooseOneOption> options, int choicesRequired) {
        this(options, false, choicesRequired);
    }

    /**
     * Encodes a modal selection for casting. Choose-one spells use a 0-based mode index;
     * choose-two (or higher) spells use a negative bitmask ({@code -(1 << mode0 | 1 << mode1 | ...)}).
     */
    public static int encodeModeSelection(int choicesRequired, int... modeIndices) {
        if (choicesRequired == 1) {
            if (modeIndices.length != 1) {
                throw new IllegalArgumentException("Choose-one requires exactly one mode index");
            }
            return modeIndices[0];
        }
        if (modeIndices.length != choicesRequired) {
            throw new IllegalArgumentException("Expected " + choicesRequired + " mode indices");
        }
        int mask = 0;
        for (int modeIndex : modeIndices) {
            mask |= (1 << modeIndex);
        }
        return -mask;
    }

    /** Returns the chosen mode indices in card-text order. */
    public List<Integer> decodeModeIndices(int xValue) {
        if (choicesRequired == 1) {
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
        if (chosen.size() != choicesRequired) {
            throw new IllegalStateException("Expected " + choicesRequired + " modes, got " + chosen.size());
        }
        return chosen;
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
     */
    public record ChooseOneOption(String label, List<CardEffect> effects, TargetFilter targetFilter,
                                  List<TargetFilter> targetFilters) {
        public ChooseOneOption(String label, CardEffect effect) {
            this(label, List.of(effect), null, null);
        }

        public ChooseOneOption(String label, CardEffect effect, TargetFilter targetFilter) {
            this(label, List.of(effect), targetFilter, null);
        }

        public ChooseOneOption(String label, List<CardEffect> effects) {
            this(label, effects, null, null);
        }

        public ChooseOneOption(String label, List<CardEffect> effects, TargetFilter targetFilter) {
            this(label, effects, targetFilter, null);
        }

        /** Multi-target mode: one target filter per effect, mapped positionally. */
        public ChooseOneOption(String label, List<CardEffect> effects, List<TargetFilter> targetFilters) {
            this(label, effects, null, targetFilters);
        }

        /** Backward-compatible accessor for single-effect modes (returns the first effect). */
        public CardEffect effect() {
            return effects.getFirst();
        }
    }
}
