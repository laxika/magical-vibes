package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Exiles a targeted card from any graveyard. If the exiled card is a creature card, the controller
 * gains {@code creatureLifeGain} life and the source permanent gets {@code creatureCountersOnSource}
 * +1/+1 counters. It can also make the card's graveyard owner lose
 * {@code creatureLifeLossToGraveyardOwner} life. If the exiled card is a noncreature card, the
 * source permanent gets +{@code noncreaturePowerBoost}/+{@code noncreatureToughnessBoost} until end
 * of turn.
 * <p>Used by Deathgorge Scavenger and Scavenging Ooze and similar cards that provide conditional
 * bonuses based on the type of card exiled from a graveyard. The target scope defaults to all
 * graveyards for the existing constructors.</p>
 *
 * @param filter optional restriction on the targeted graveyard card
 */
public record ExileGraveyardCardWithConditionalBonusEffect(
        int creatureLifeGain,
        int creatureCountersOnSource,
        int noncreaturePowerBoost,
        int noncreatureToughnessBoost,
        int creatureLifeLossToGraveyardOwner,
        GraveyardSearchScope graveyardScope,
        CardPredicate filter
) implements CardEffect {

    public ExileGraveyardCardWithConditionalBonusEffect(
            int creatureLifeGain,
            int creatureCountersOnSource,
            int noncreaturePowerBoost,
            int noncreatureToughnessBoost
    ) {
        this(creatureLifeGain, creatureCountersOnSource, noncreaturePowerBoost,
                noncreatureToughnessBoost, 0, GraveyardSearchScope.ALL_GRAVEYARDS, null);
    }

    /**
     * Convenience constructor for cards whose creature-card branch only gains life.
     */
    public ExileGraveyardCardWithConditionalBonusEffect(int creatureLifeGain, int noncreaturePowerBoost, int noncreatureToughnessBoost) {
        this(creatureLifeGain, 0, noncreaturePowerBoost, noncreatureToughnessBoost);
    }

    public static ExileGraveyardCardWithConditionalBonusEffect creatureCardOwnerLosesLife(int amount) {
        return new ExileGraveyardCardWithConditionalBonusEffect(
                0, 0, 0, 0, amount, GraveyardSearchScope.OPPONENT_GRAVEYARD, null);
    }

    public static ExileGraveyardCardWithConditionalBonusEffect creatureCardOnly(int lifeGain) {
        return new ExileGraveyardCardWithConditionalBonusEffect(
                lifeGain, 0, 0, 0, 0, GraveyardSearchScope.ALL_GRAVEYARDS,
                new CardTypePredicate(CardType.CREATURE));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(filter == null
                ? TargetPredicates.graveyardCard(graveyardScope)
                : TargetPredicates.graveyardCards(filter, graveyardScope));
    }
}
