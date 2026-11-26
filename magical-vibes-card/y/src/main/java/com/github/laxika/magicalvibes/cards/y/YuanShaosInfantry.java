package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "129")
public class YuanShaosInfantry extends Card {

    public YuanShaosInfantry() {
        // Whenever this creature attacks alone, this creature can't be blocked this combat.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                MakeCreatureUnblockableEffect.self(Duration.UNTIL_END_OF_COMBAT));
    }
}
