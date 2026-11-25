package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "136")
public class FireSages extends Card {

    public FireSages() {
        addTrigger(Triggers.attacks(), new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                "{1}{R}{R}: Put a +1/+1 counter on this creature."
        ));
    }
}
