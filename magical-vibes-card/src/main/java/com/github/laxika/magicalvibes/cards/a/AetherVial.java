package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandWithManaValueEqualToSourceCountersEffect;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "91")
public class AetherVial extends Card {

    public AetherVial() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.CHARGE),
                        "Put a charge counter on Aether Vial?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCreatureFromHandWithManaValueEqualToSourceCountersEffect(CounterType.CHARGE)),
                "{T}: You may put a creature card with mana value equal to the number of charge counters on Aether Vial from your hand onto the battlefield."
        ));
    }
}
