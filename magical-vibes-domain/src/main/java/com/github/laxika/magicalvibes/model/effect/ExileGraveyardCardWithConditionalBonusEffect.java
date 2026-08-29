package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles a targeted card from any graveyard. If the exiled card is a creature card, the controller
 * gains {@code creatureLifeGain} life and the source permanent gets {@code creatureCountersOnSource}
 * +1/+1 counters. If the exiled card is a noncreature card, the source permanent gets
 * +{@code noncreaturePowerBoost}/+{@code noncreatureToughnessBoost} until end of turn.
 *
 * <p>Used by Deathgorge Scavenger and Scavenging Ooze and similar cards that provide conditional
 * bonuses based on the type of card exiled from a graveyard. The optional {@code filter} narrows
 * which graveyard cards may be targeted.</p>
 */
public record ExileGraveyardCardWithConditionalBonusEffect(
        int creatureLifeGain,
        int creatureCountersOnSource,
        int noncreaturePowerBoost,
        int noncreatureToughnessBoost,
        CardPredicate filter
) implements CardEffect {

    public ExileGraveyardCardWithConditionalBonusEffect(int creatureLifeGain,
                                                         int creatureCountersOnSource,
                                                         int noncreaturePowerBoost,
                                                         int noncreatureToughnessBoost) {
        this(creatureLifeGain, creatureCountersOnSource, noncreaturePowerBoost,
                noncreatureToughnessBoost, null);
    }

    /**
     * Convenience constructor for cards whose creature-card branch only gains life.
     */
    public ExileGraveyardCardWithConditionalBonusEffect(int creatureLifeGain, int noncreaturePowerBoost, int noncreatureToughnessBoost) {
        this(creatureLifeGain, 0, noncreaturePowerBoost, noncreatureToughnessBoost, null);
    }

    @Override
    public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.ALL_GRAVEYARDS)); }
}
