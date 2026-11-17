package com.github.laxika.magicalvibes.model.effect;

/**
 * Sen Triplets' upkeep ability, applied to the stack entry's target player (an opponent). This turn
 * that player can't cast spells or activate abilities and plays with their hand revealed, and the
 * controller may play lands and cast spells from that player's hand.
 *
 * <p>A single cohesive effect (not three composable ones) because all three clauses must apply to the
 * <em>same</em> chosen opponent — splitting them would prompt for a separate target per clause.
 * Resolution makes a floating effect of this record on the target player, which neither casts spells nor activates
 * abilities until end of turn; the effect's controller may play lands and cast spells from that player's hand
 * meanwhile ({@code GameData.canPlayFromHandOf}).
 */
public record SenTripletsUpkeepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
