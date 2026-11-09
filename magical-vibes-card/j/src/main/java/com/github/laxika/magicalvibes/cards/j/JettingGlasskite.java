package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "BOK", collectorNumber = "38")
@CardRegistration(set = "EMA", collectorNumber = "58")
public class JettingGlasskite extends Card {

    public JettingGlasskite() {
        // Flying is auto-loaded from Scryfall.

        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                new CounterSpellEffect());
    }
}
