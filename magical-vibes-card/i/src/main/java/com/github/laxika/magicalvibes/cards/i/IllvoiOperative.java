package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "61")
public class IllvoiOperative extends Card {

    public IllvoiOperative() {
        addTrigger(Triggers.youCastSpell(), new NthSpellCastTriggerEffect(
                2,
                List.of(new PutCountersOnSourceEffect(1, 1, 1))
        ));
    }
}
