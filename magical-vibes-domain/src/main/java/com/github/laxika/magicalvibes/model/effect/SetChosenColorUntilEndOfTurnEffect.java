package com.github.laxika.magicalvibes.model.effect;

/**
 * Target permanent becomes a color the controller chooses until end of turn (layer 5);
 * the spell-capable form also supports a target spell. On resolution the controller is prompted
 * for a color, and the choice handler applies the same floating color-setting effect as
 * {@link GrantColorUntilEndOfTurnEffect}. Used by Distorting Lens, Blind Seer, and self-scoped
 * color-changing abilities.
 */
public record SetChosenColorUntilEndOfTurnEffect(boolean canTargetSpell, boolean targeted) implements CardEffect {

    public SetChosenColorUntilEndOfTurnEffect() {
        this(false, true);
    }

    public SetChosenColorUntilEndOfTurnEffect(boolean canTargetSpell) {
        this(canTargetSpell, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (!targeted) {
            return TargetSpec.NONE;
        }
        TargetPredicate target = canTargetSpell
                ? TargetPredicates.anyOf(TargetPredicates.permanent(), TargetPredicates.spellOnStack())
                : TargetPredicates.permanent();
        return TargetSpec.benign(target);
    }
}
