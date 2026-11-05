package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "116")
public class MaChaoWesternWarrior extends Card {

    public MaChaoWesternWarrior() {
        // Horsemanship is auto-loaded from Scryfall keywords.
        // Whenever Ma Chao attacks alone, it can't be blocked this combat.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new MakeCreatureUnblockableEffect(true, false, EffectDuration.UNTIL_END_OF_COMBAT));
    }
}
