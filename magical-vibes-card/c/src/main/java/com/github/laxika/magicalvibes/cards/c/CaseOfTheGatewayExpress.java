package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "8")
public class CaseOfTheGatewayExpress extends Card {

    public CaseOfTheGatewayExpress() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ControlledCreaturesDealDamageToTargetEffect(1, new PermanentIsCreaturePredicate()));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                        new AttackedWithCreaturesThisTurn(3),
                                        new NotCondition(new SourceIsSolved())
                                ))),
                new SolveSourceEffect());

        addStaticEffect(new ConditionalEffect(new SourceIsSolved(),
                        new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES)));
    }
}
