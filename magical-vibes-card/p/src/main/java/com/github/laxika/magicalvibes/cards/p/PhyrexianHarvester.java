package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "13")
public class PhyrexianHarvester extends Card {

    public PhyrexianHarvester() {
        addTrigger(Triggers.dealtDamage(),
                new SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect(
                        new EventValue(), new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
