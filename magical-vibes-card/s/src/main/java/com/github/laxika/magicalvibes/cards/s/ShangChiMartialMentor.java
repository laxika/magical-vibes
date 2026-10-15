package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "732")
public class ShangChiMartialMentor extends Card {

    public ShangChiMartialMentor() {
        addEffect(EffectSlot.STATIC, new DoublePlusOnePlusOneCountersEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                "Power-up — {5}{G}{G}: Put three +1/+1 counters on Shang-Chi. Activate each power-up ability "
                        + "only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}
