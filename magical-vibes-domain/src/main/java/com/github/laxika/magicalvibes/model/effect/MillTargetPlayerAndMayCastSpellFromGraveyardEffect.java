package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Target opponent mills {@code count} cards, then the controller may cast an instant or sorcery
 * spell from that opponent's graveyard without paying its mana cost. If that spell would be put
 * into a graveyard, it is exiled instead.
 */
public record MillTargetPlayerAndMayCastSpellFromGraveyardEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public PlayerRelation targetPlayerRelation() {
        return PlayerRelation.OPPONENT;
    }
}
