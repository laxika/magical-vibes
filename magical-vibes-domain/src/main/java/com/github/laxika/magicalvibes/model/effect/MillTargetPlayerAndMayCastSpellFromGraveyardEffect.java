package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/**
 * Target opponent mills {@code count} cards, then the controller may cast an instant or sorcery
 * spell from that opponent's graveyard without paying its mana cost. If that spell would be put
 * into a graveyard, it is exiled instead.
 */
public record MillTargetPlayerAndMayCastSpellFromGraveyardEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.players(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT)));
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
