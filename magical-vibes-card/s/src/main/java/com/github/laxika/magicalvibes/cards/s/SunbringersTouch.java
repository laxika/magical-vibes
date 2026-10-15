package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "DTK", collectorNumber = "209")
public class SunbringersTouch extends Card {

    public SunbringersTouch() {
        // Bolster X, where X is the number of cards in your hand.
        addEffect(EffectSlot.SPELL, new BolsterEffect(new CardsInHand(CountScope.CONTROLLER)));
        // Each creature you control with a +1/+1 counter on it gains trample until end of turn.
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
