package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player exiles cards from the top of their library until an instant or sorcery card is
 * found. The controller may cast that card without paying its mana cost, then the remaining cards
 * are put on the bottom of that library in a random order.
 * When used by a combat-damage trigger, the damaged player supplies the target automatically.
 */
public record RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect()
        implements CombatDamageTriggerContextEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
