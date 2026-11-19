package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "83")
public class VenarianGold extends Card {

    public VenarianGold() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(Subjects.target()),
                        new PutCounterOnTargetPermanentEffect(Subjects.attached(), CounterType.SLEEP, new XValue()));

        addStaticEffect(DoesntUntapWithCounterEffect.enchantedWithCounterOnEnchantedPermanent(CounterType.SLEEP));
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                new RemoveCounterFromSourceEffect(Subjects.attachedAtTrigger(), CounterType.SLEEP));
    }
}
