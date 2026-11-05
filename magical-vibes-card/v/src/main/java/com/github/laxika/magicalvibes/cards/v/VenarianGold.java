package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "83")
public class VenarianGold extends Card {

    public VenarianGold() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnReferencedPermanentEffect(CounterType.SLEEP, new XValue()));

        addStaticEffect(DoesntUntapWithCounterEffect.enchantedWithCounterOnEnchantedPermanent(CounterType.SLEEP));
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                new RemoveCounterFromReferencedPermanentEffect(CounterType.SLEEP));
    }
}
