package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmplifyEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ARB", collectorNumber = "131")
@CardRegistration(set = "HOP", collectorNumber = "96")
public class ArsenalThresher extends Card {

    public ArsenalThresher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new AmplifyEffect(1, new CardTypePredicate(CardType.ARTIFACT)));
    }
}