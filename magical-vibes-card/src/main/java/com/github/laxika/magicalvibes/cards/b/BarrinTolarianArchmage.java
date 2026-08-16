package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.PermanentPutIntoYourHandFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "45")
public class BarrinTolarianArchmage extends Card {

    public BarrinTolarianArchmage() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate())),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "Target must be another creature or planeswalker"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnToHandEffect.target());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new PermanentPutIntoYourHandFromBattlefieldThisTurn(), new DrawCardEffect(1)));
    }
}
