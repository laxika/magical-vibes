package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CreatureCardsPutIntoGraveyardThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "79")
public class CaseOfTheGorgonsKiss extends Card {

    public CaseOfTheGorgonsKiss() {
        target(new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentDealtDamageThisTurnPredicate())),
                        "Target must be a creature that was dealt damage this turn"), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new AllOf(List.of(
                        new CreatureCardsPutIntoGraveyardThisTurnAtLeast(3),
                        new NotCondition(new SourceIsSolved())
                )), new SolveSourceEffect()));

        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceIsSolved(),
                        new AnimatePermanentsEffect(4, 4,
                                List.of(CardSubtype.GORGON),
                                Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK),
                                null,
                                Set.of(CardType.CREATURE),
                                GrantScope.SELF,
                                EffectDuration.PERMANENT)));
    }
}
