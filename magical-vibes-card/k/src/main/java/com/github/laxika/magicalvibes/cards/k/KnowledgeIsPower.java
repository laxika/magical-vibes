package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MKC", collectorNumber = "42")
@CardRegistration(set = "MKC", collectorNumber = "352")
public class KnowledgeIsPower extends Card {

    public KnowledgeIsPower() {
        CardsDrawnThisTurn cardsDrawn = new CardsDrawnThisTurn();
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                cardsDrawn, cardsDrawn, GrantScope.OWN_CREATURES));
    }
}
