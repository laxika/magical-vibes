package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "122")
@CardRegistration(set = "C14", collectorNumber = "170")
public class XathridDemon extends Card {

    public XathridDemon() {
        // "Other than this creature" is this object: a Demon that left and came back may be sacrificed (CR 400.7).
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))), Subjects.you()),
                new ConditionalEffect(HappenedThisWay.sacrificed(),
                        new LoseLifeEffect(AmountThisWay.sacrificedPower(), Subjects.eachOpponent())),
                new ConditionalEffect(new NotCondition(HappenedThisWay.sacrificed()), SequenceEffect.of(
                        new TapPermanentsEffect(Subjects.source()),
                        new LoseLifeEffect(7, Subjects.you())))));
    }
}
