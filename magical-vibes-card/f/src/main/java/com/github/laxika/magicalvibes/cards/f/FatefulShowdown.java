package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;

@CardRegistration(set = "KLD", collectorNumber = "114")
@CardRegistration(set = "KLR", collectorNumber = "123")
public class FatefulShowdown extends Card {

    public FatefulShowdown() {
        addSpellEffect(DealDamageEffect.toAnyTarget(new CardsInHand(CountScope.CONTROLLER)));
        addSpellEffect(new DiscardOwnHandThenDrawThatManyEffect());
    }
}
