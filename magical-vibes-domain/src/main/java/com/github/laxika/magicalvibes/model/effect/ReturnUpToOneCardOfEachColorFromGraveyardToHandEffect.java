package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * "For each color, return up to one target card of that color from your graveyard to your hand" (All Suns' Dawn), "return
 * up to one target instant card and up to one target sorcery card from your graveyard to your hand" (Shreds of Sanity):
 * the cards chosen for all the card's target groups return together. What each group may target, and that one card
 * can't fill two, is the card's target declaration.
 */
public record ReturnUpToOneCardOfEachColorFromGraveyardToHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
