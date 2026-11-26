package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "139")
public class HandThatFeeds extends Card {

    public HandThatFeeds() {
        // "attacks while there are four or more card types" qualifies the event and is not an
        // intervening "if", so delirium is checked only when the creature attacks.
        addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withEventCondition(
                new Delirium()), SequenceEffect.of(
                        BoostEffect.self(2, 0),
                        new GrantKeywordEffect(Keyword.MENACE, Subjects.source())));
    }
}
