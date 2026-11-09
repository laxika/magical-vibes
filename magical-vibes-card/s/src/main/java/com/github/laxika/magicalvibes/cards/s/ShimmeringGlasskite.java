package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "BOK", collectorNumber = "51")
public class ShimmeringGlasskite extends Card {

    public ShimmeringGlasskite() {
        // Flying is auto-loaded from Scryfall.

        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                new CounterSpellEffect());
    }
}
