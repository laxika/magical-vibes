package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "50")
@CardRegistration(set = "FDN", collectorNumber = "152")
@CardRegistration(set = "GN3", collectorNumber = "24")
@CardRegistration(set = "PIO", collectorNumber = "48")
public class BrinebornCutthroat extends Card {

    public BrinebornCutthroat() {
        // Whenever you cast a spell during an opponent's turn, put a +1/+1 counter on this creature.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                true
        ));
    }
}
