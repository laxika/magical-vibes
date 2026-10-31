package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantMayhemToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceGraveyardSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class GreenGoblin extends Card {

    public GreenGoblin() {
        addStaticEffect(new ReduceGraveyardSpellCastCostEffect(2));
        addStaticEffect(new GrantMayhemToGraveyardCardsEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
