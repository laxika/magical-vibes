package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;

@CardRegistration(set = "KLD", collectorNumber = "114")
public class FatefulShowdown extends Card {

    public FatefulShowdown() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetEffect(new CardsInHand(CountScope.CONTROLLER)));
        addEffect(EffectSlot.SPELL, new DiscardOwnHandThenDrawThatManyEffect());
    }
}
