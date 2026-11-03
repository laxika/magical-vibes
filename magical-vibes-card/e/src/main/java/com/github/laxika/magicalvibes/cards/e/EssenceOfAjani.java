package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutResolvingSpellIntoCommandZoneEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "277")
@CardRegistration(set = "MB2", collectorNumber = "513")
public class EssenceOfAjani extends Card {

    public EssenceOfAjani() {
        addSpellEffect(new PutResolvingSpellIntoCommandZoneEffect());
        addTrigger(new TriggerCondition(Triggers.youCastSpell().anySpell(), AbilityZones.COMMAND),
                TriggerOptions.DEFAULT,
                new GainLifeEffect(1));
    }
}
