package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a targeted card from any graveyard. If the exiled card is a creature card, the controller
 * gains {@code creatureLifeGain} life and the source permanent gets {@code creatureCountersOnSource}
 * +1/+1 counters. If the exiled card is a noncreature card, the source permanent gets
 * +{@code noncreaturePowerBoost}/+{@code noncreatureToughnessBoost} until end of turn.
 *
 * <p>Used by Deathgorge Scavenger and Scavenging Ooze and similar cards that provide conditional
 * bonuses based on the type of card exiled from a graveyard.</p>
 */
public record ExileGraveyardCardWithConditionalBonusEffect(
        int creatureLifeGain,
        int creatureCountersOnSource,
        int noncreaturePowerBoost,
        int noncreatureToughnessBoost
) implements CardEffect {

    /**
     * Convenience constructor for cards whose creature-card branch only gains life.
     */
    public ExileGraveyardCardWithConditionalBonusEffect(int creatureLifeGain, int noncreaturePowerBoost, int noncreatureToughnessBoost) {
        this(creatureLifeGain, 0, noncreaturePowerBoost, noncreatureToughnessBoost);
    }

    @Override
    public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.anyGraveyardCard()); }
}
