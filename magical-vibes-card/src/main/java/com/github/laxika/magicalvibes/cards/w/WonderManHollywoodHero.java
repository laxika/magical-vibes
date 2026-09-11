package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowExtraPowerUpActivationEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "160")
public class WonderManHollywoodHero extends Card {

    public WonderManHollywoodHero() {
        addEffect(EffectSlot.STATIC, new AllowExtraPowerUpActivationEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}{R}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "Power-up — {5}{R}{R}: Put two +1/+1 counters on Wonder Man. Activate each power-up ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}
