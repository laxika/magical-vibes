package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.TotalCountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "126")
@CardRegistration(set = "SPM", collectorNumber = "270")
@CardRegistration(set = "OM1", collectorNumber = "129")
public class CheeringCrowd extends Card {

    public CheeringCrowd() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.ANY),
                UnlessPaysEffect.mayPay(Subjects.activePlayer(),
                        new PutTypedCounterOnSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new AwardManaToActivePlayerEffect(ManaColor.COLORLESS, new TotalCountersOnSource())));
    }
}
