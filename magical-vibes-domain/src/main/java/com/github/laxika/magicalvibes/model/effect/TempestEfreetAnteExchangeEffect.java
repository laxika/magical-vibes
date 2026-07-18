package com.github.laxika.magicalvibes.model.effect;

/**
 * Tempest Efreet's activated ante ability. The targeted opponent may pay {@code lifeCost} life; if
 * that player doesn't (or can't), they reveal a card at random from their hand, that revealed card
 * is put into the ability's controller's hand, and Tempest Efreet is put into that player's
 * graveyard.
 *
 * <p>The oracle "Exchange ownership … This change in ownership is permanent" is an ante concept: the
 * permanent, cross-game transfer of card ownership is outside a single game's scope and is not
 * modeled. Within one game this effect resolves to the observable zone movements only (the revealed
 * card into the controller's hand, Tempest Efreet into the opponent's graveyard); the {@code ownerId}
 * stamped at game setup is frozen and left unchanged.
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
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
