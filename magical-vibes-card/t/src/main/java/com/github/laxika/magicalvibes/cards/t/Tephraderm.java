package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceCreatureOrSpellControllerEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "239")
public class Tephraderm extends Card {

    public Tephraderm() {
        addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,
                new DealDamageToDamageSourceCreatureOrSpellControllerEffect());
    }
}
