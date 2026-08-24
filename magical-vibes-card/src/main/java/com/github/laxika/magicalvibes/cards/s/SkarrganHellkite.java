package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "114")
public class SkarrganHellkite extends Card {

    public SkarrganHellkite() {
        addEffect(EffectSlot.STATIC, new RiotEffect());

        // {3}{R}: This creature deals 2 damage divided as you choose among one or two targets.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new DealDividedDamageEffect(
                        new Fixed(2), null, DivisionMode.CHOSEN, null, 2, true, false, false)),
                "{3}{R}: This creature deals 2 damage divided as you choose among one or two targets.",
                null, null, null, null, List.of(), 1, 2
        ).withRequiredSourceCounters(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
