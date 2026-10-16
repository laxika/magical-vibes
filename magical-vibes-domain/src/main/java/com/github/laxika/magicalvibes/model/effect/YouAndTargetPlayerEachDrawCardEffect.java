package com.github.laxika.magicalvibes.model.effect;

/**
 * "You and target player each draw {@code amount} card(s)." The two players draw in turn order
 * starting with the active player (APNAP), like {@link EachPlayerDrawsCardEffect}.
 */
public record YouAndTargetPlayerEachDrawCardEffect(int amount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
