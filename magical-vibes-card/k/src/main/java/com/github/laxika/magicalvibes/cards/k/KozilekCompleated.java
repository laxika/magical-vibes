package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DefendingPlayerPoisonCounters;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "266")
@CardRegistration(set = "MB2", collectorNumber = "502")
public class KozilekCompleated extends Card {

    public KozilekCompleated() {
        addTrigger(Triggers.youCastThisSpell(),
                new GivePoisonCountersEffect(2, Subjects.eachOpponent()),
                DiscardEffect.eachEvaluatedFor(
                        new Max(new Fixed(0), new Sum(new CardsInHand(CountScope.CONTROLLER), new Fixed(-2))),
                        Subjects.eachOpponent()));
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                new DefendingPlayerPoisonCounters(), new PermanentTruePredicate(),
                Subjects.defendingPlayer()));
    }
}
