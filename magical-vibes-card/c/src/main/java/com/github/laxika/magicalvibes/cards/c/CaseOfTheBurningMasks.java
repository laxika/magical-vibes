package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerControlledSourcesDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "113")
public class CaseOfTheBurningMasks extends Card {

    public CaseOfTheBurningMasks() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DealDamageToTargetCreatureEffect(3));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                        new ControllerControlledSourcesDealtDamageThisTurn(3),
                                        new NotCondition(new SourceIsSolved())
                                ))),
                new SolveSourceEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new ExileTopCardsChooseOneMayPlayThisTurnEffect(3)),
                "Sacrifice this Case: Exile the top three cards of your library. Choose one of them. "
                        + "You may play that card this turn."
        ).withActivationCondition(new SourceIsSolved(), "Activate only if this Case is solved."));
    }
}
