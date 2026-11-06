package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile up to as many cards from the damaged player's graveyard as combat damage was dealt.
 * Creature cards exiled this way produce +1/+1 counters on the damage-dealing permanent, while
 * noncreature cards produce life gain for the ability's controller.
 *
 * <p>"That many" is the damage the trigger's event dealt: the collector passes it to
 * {@link #graveyardChoiceMaxTargets(int)} as the targets are chosen.
 */
public record ExileUpToCombatDamageCardsFromGraveyardEffect() implements GraveyardCardChoosingEffect {

    @Override
    public int graveyardChoiceMaxTargets() {
        return 0;
    }

    @Override
    public int graveyardChoiceMaxTargets(int eventValue) {
        return Math.max(0, eventValue);
    }
}
