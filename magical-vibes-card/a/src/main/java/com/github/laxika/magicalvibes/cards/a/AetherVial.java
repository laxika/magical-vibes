package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandWithManaValueEqualToSourceCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "91")
@CardRegistration(set = "V10", collectorNumber = "1")
@CardRegistration(set = "MMA", collectorNumber = "197")
@CardRegistration(set = "IMA", collectorNumber = "212")
@CardRegistration(set = "MPS", collectorNumber = "6")
@CardRegistration(set = "SLD", collectorNumber = "1640")
@CardRegistration(set = "2X2", collectorNumber = "298")
@CardRegistration(set = "MB2", collectorNumber = "216")
public class AetherVial extends Card {

    public AetherVial() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
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
