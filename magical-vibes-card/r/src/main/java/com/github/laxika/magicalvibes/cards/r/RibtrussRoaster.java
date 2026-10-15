package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "43")
@CardRegistration(set = "SOC", collectorNumber = "91")
public class RibtrussRoaster extends Card {

    public RibtrussRoaster() {
        // Devour 1 (As this creature enters, you may sacrifice any number of creatures.
        // It enters with that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DevourEffect(1));

        // At the beginning of your end step, create a number of 1/1 black and green Pest
        // creature tokens equal to the number of +1/+1 counters on this creature. They have
        // "When this token dies, you gain 1 life."
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new CreateTokenEffect(
                CardType.CREATURE, new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_DEATH, new GainLifeEffect(1)),
                List.of(), false, false, false, 0, Set.of()));
    }
}
