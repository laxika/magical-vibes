package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "217")
public class KangTemporalTyrant extends Card {

    public KangTemporalTyrant() {
        addTrigger(Triggers.attacks(), new DrawDiscardAndConniveEffect());
        addTrigger(Triggers.youDrawSecondCardEachTurn(), SequenceEffect.of(
                new LoseLifeEffect(1, Subjects.eachOpponent()),
                new GainLifeEffect(1)));
    }
}
