package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "WAR", collectorNumber = "199")
public class HeartwarmingRedemption extends Card {

    public HeartwarmingRedemption() {
        addSpellEffect(new DiscardOwnHandThenDrawThatManyEffect());
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new GainLifeEffect(new CardsInHand(CountScope.CONTROLLER)));
    }
}
