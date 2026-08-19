package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals a random card from one target player's hand, then gives one target creature +X/+X or
 * -X/-X until end of turn, where X is the revealed card's mana value.
 *
 * <p>The player and creature target groups are separate because the revealed card's mana value is
 * only known when the effect resolves. The default groups are 0 and 1.</p>
 */
public record RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffect(
        int targetPlayerGroup, int targetCreatureGroup, boolean positive) implements CardEffect {

    public RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffect() {
        this(0, 1, false);
    }

    public RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffect(
            int targetPlayerGroup, int targetCreatureGroup) {
        this(targetPlayerGroup, targetCreatureGroup, false);
    }

    public RevealRandomCardFromTargetPlayerHandBoostTargetCreatureByManaValueEffect(boolean positive) {
        this(0, 1, positive);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
