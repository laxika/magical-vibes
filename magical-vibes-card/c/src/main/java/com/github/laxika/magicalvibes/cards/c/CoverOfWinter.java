package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamagePerSourceToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "3")
public class CoverOfWinter extends Card {

    public CoverOfWinter() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{S}"));
        addStaticEffect(new PreventXDamagePerSourceToControllerAndCreaturesEffect(
                new CountersOnSource(CounterType.AGE), true, true));

        addActivatedAbility(new ActivatedAbility(false, "{S}",
                List.of(new PutCountersOnSelfEffect(CounterType.AGE)),
                "{S}: Put an age counter on this enchantment."));
    }
}
