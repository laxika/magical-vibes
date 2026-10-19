package com.github.laxika.magicalvibes.model.effect;

/**
 * Tempest Efreet's activated ante ability. The targeted opponent may pay {@code lifeCost} life; if
 * that player doesn't (or can't), they reveal a card at random from their hand, that revealed card
 * is put into the ability's controller's hand, and Tempest Efreet is put into that player's
 * graveyard.
 *
 * <p>The oracle "Exchange ownership … This change in ownership is permanent" is modeled within one
 * game: the revealed card moves to the controller's hand and Tempest Efreet to the opponent's graveyard,
 * and the two cards swap owners in {@code GameData.cardOwnerOverrides}, so the revealed card later
 * returns to its new owner's hand or graveyard. The permanent, cross-game transfer an ante implies is
 * outside a single game's scope and is not modeled.
 *
 * <p>Targets the opponent player — the ability declares "target opponent" through a
 * {@code PlayerRelationPredicate}; {@link #targetSpec()} declares the player category. The paying
 * player is the stack entry's target; the exchange reads the controller and source card from the
 * stack entry / pending may-ability.
 *
 * @param lifeCost how much life the targeted opponent may pay to avoid the exchange (10)
 */
public record TempestEfreetAnteExchangeEffect(int lifeCost) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
