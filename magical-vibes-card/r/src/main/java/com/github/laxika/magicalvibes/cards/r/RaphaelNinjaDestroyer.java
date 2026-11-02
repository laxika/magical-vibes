package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "102")
@CardRegistration(set = "TMT", collectorNumber = "218")
@CardRegistration(set = "TMT", collectorNumber = "303")
public class RaphaelNinjaDestroyer extends Card {

    public RaphaelNinjaDestroyer() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());
        addTrigger(Triggers.dealtDamage(),
                new AwardPersistentManaEffect(ManaColor.RED, new EventValue()));
    }
}
