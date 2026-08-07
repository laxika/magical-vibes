package com.github.laxika.magicalvibes.model.effect;

/**
 * Phyrexian Grimoire: "Target opponent chooses one of the top two cards of your graveyard. Exile
 * that card and put the other one into your hand."
 *
 * <p>Resolved by {@code OpponentChoosesOneOfTopTwoGraveyardCardsEffectHandler}. The cards are not
 * targeted — only the opponent is — so the pick is a resolution-time choice. With a single card in
 * the graveyard that card is exiled and nothing goes to hand; with an empty graveyard nothing
 * happens.
 */
public record OpponentChoosesOneOfTopTwoGraveyardCardsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
