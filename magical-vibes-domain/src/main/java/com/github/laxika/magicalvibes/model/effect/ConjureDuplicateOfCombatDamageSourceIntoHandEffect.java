package com.github.laxika.magicalvibes.model.effect;

/** Conjures a duplicate of the nontoken creature that dealt the combat damage into hand. */
public record ConjureDuplicateOfCombatDamageSourceIntoHandEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
