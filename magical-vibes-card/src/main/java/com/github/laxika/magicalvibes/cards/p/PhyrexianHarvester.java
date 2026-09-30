package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YONE", collectorNumber = "13")
public class PhyrexianHarvester extends Card {

    public PhyrexianHarvester() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(
                        new EventValue(), new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
