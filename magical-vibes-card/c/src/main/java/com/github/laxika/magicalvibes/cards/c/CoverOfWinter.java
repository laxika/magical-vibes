package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamagePerSourceToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "3")
public class CoverOfWinter extends Card {

    public CoverOfWinter() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{S}"));
        addEffect(EffectSlot.STATIC, new PreventXDamagePerSourceToControllerAndCreaturesEffect(
                new CountersOnSource(CounterType.AGE), true, true));

        addActivatedAbility(new ActivatedAbility(false, "{S}",
                List.of(new PutCountersOnSelfEffect(CounterType.AGE)),
                "{S}: Put an age counter on this enchantment."));
    }
}
