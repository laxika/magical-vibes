package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Returns targeted opponent creature cards as face-down Cybermen under the effect controller's control. */
public record ReturnTargetCreatureCardsFromGraveyardToBattlefieldFaceDownAsCybermenEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.OPPONENT_GRAVEYARD));
    }
}
