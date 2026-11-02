package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "121")
public class SphereOfAnnihilation extends Card {

    public SphereOfAnnihilation() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.VOID, new XValue()));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect(
                        CounterType.VOID));
    }
}
