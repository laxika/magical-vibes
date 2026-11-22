package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "39")
public class MaelstromDjinn extends Card {

    public MaelstromDjinn() {
        addMorph("{2}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new PutCountersOnSelfEffect(CounterType.TIME, 2),
                new GrantKeywordEffect(Keyword.VANISHING, Subjects.source(), Duration.PERMANENT),
                new GrantEffectToTargetEffect(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                        TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                        new RemoveCounterFromSourceEffect(CounterType.TIME, 1)), Duration.PERMANENT, false, Subjects.source()));
    }
}
