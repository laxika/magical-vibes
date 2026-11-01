package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "39")
public class MaelstromDjinn extends Card {

    public MaelstromDjinn() {
        addMorph("{2}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new PutCountersOnSelfEffect(CounterType.TIME, 2),
                new GrantKeywordEffect(Keyword.VANISHING, GrantScope.SELF, GrantDuration.INDEFINITE),
                new GrantEffectToSourceEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME)));
    }
}
