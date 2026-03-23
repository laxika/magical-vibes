package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a targeted card from any graveyard. If the exiled card is a creature card, the controller
 * gains {@code creatureLifeGain} life. If the exiled card is a noncreature card, the source
 * permanent gets +{@code noncreaturePowerBoost}/+{@code noncreatureToughnessBoost} until end of turn.
 *
 * <p>Used by Deathgorge Scavenger and similar cards that provide conditional bonuses based on
 * the type of card exiled from a graveyard.</p>
 */
public record ExileGraveyardCardWithConditionalBonusEffect(
        int creatureLifeGain,
        int noncreaturePowerBoost,
        int noncreatureToughnessBoost
) implements CardEffect {

    @Override
    public boolean canTargetGraveyard() { return true; }

    @Override
    public boolean canTargetAnyGraveyard() { return true; }
}
