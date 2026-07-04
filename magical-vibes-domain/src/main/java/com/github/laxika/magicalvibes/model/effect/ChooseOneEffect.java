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
public record ChooseOneEffect(List<ChooseOneOption> options, boolean optional) implements CardEffect {

    public ChooseOneEffect(List<ChooseOneOption> options) {
        this(options, false);
    }

    /**
     * A single selectable mode. A mode may resolve one or more effects (e.g. "Surveil 2, then draw
     * a card" or "each opponent loses 3 life and you gain 3 life"); when chosen, all of the mode's
     * effects are spliced into the spell's resolution in order. The optional {@code targetFilter}
     * overrides the spell's cast-time target filter for this mode.
     */
    public record ChooseOneOption(String label, List<CardEffect> effects, TargetFilter targetFilter) {
        public ChooseOneOption(String label, CardEffect effect) {
            this(label, List.of(effect), null);
        }

        public ChooseOneOption(String label, CardEffect effect, TargetFilter targetFilter) {
            this(label, List.of(effect), targetFilter);
        }

        public ChooseOneOption(String label, List<CardEffect> effects) {
            this(label, effects, null);
        }

        /** Backward-compatible accessor for single-effect modes (returns the first effect). */
        public CardEffect effect() {
            return effects.getFirst();
        }
    }
}
