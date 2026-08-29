package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Causes the source permanent to become a copy of a target creature card in its controller's
 * graveyard whose mana value equals the activation's X value, retaining the source's own ability.
 */
public record BecomeCopyOfTargetCreatureCardInGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
