package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "121")
@CardRegistration(set = "IMA", collectorNumber = "209")
@CardRegistration(set = "VMA", collectorNumber = "262")
@CardRegistration(set = "DMR", collectorNumber = "202")
public class Spiritmonger extends Card {

    public Spiritmonger() {
        addTrigger(Triggers.dealsDamageToCreature(),
                new PutCountersOnSourceEffect(1, 1, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SetChosenColorUntilEndOfTurnEffect(false, false)),
                "{G}: This creature becomes the color of your choice until end of turn."
        ));
    }
}
