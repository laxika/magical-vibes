package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "77")
public class SlithBloodletter extends Card {

    public SlithBloodletter() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Slith Bloodletter."));
    }
}
