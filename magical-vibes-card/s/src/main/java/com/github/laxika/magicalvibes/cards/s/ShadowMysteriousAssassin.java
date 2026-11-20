package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "50")
@CardRegistration(set = "FIC", collectorNumber = "148")
public class ShadowMysteriousAssassin extends Card {

    public ShadowMysteriousAssassin() {
        PermanentPredicate anotherNonlandPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                new PermanentNotPredicate(new PermanentIsLandPredicate())
        ));

        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, anotherNonlandPermanent, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), SequenceEffect.of(
                                new DrawCardEffect(2),
                                new LoseLifeEffect(
                                        AmountThisWay.sacrificedManaValue(),
                                        Subjects.eachOpponent())))),
                "Sacrifice another nonland permanent?"));
    }
}
