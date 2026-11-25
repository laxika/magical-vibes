package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "68")
@CardRegistration(set = "ME1", collectorNumber = "45")
public class PsychicPurge extends Card {

    public PsychicPurge() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                new LoseLifeEffect(5, Subjects.eachOpponent()));
    }
}
