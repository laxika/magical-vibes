package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "693")
public class JackOfHeartsVolatileHero extends Card {

    public JackOfHeartsVolatileHero() {
        addEffect(EffectSlot.ON_DEATH, new MassDamageEffect(new SourcePower(), false));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}{R}",
                List.of(
                        new DiscardHandEffect(),
                        new DrawCardEffect(3),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "Power-up — {4}{R}{R}: Discard your hand, then draw three cards. Put two +1/+1 counters on Jack of Hearts. "
                        + "Activate each power-up ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}
