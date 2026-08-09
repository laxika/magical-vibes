package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "UDS", collectorNumber = "17")
public class ScentOfJasmine extends Card {

    public ScentOfJasmine() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(
                new MatchingCardsInHand(CountScope.CONTROLLER, new CardColorPredicate(CardColor.WHITE)),
                2
        )));
    }
}
