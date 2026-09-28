package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YDFT", collectorNumber = "21")
public class AriusFlybyTrawler extends Card {

    public AriusFlybyTrawler() {
        addEffect(EffectSlot.ON_ATTACK,
                new SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(
                        new CardNotPredicate(new CardSubtypePredicate(CardSubtype.SHARK))));
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
