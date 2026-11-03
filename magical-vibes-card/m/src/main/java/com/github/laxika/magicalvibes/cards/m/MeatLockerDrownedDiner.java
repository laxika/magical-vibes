package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "65")
public class MeatLockerDrownedDiner extends Card {

    public MeatLockerDrownedDiner() {
        setRoomDoorManaCosts(List.of("{2}{U}", "{3}{U}{U}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Meat Locker", List.of())
                        .withManaCost("{2}{U}"),
                new ChooseOneEffect.ChooseOneOption("Drowned Diner", List.of())
                        .withManaCost("{3}{U}{U}")
        )));

        target(TargetFilters.creature(), 0, 1).addTrigger(Triggers.doorUnlocked(0),
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN, 2)));

        addTrigger(Triggers.doorUnlocked(1),
                SequenceEffect.of(
                        new DrawCardEffect(3),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
    }
}
