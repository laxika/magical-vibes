package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "10")
public class CaseOfTheUneatenFeast extends Card {

    public CaseOfTheUneatenFeast() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), new GainLifeEffect(1));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(
                        new AllOf(List.of(
                                new GainedLifeThisTurn(5),
                                new NotCondition(new SourceIsSolved())
                        )),
                        new SolveSourceEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AllowCastMatchingCardsFromGraveyardThisTurnEffect(
                                new CardTypePredicate(CardType.CREATURE))
                ),
                "Sacrifice this Case: Creature cards in your graveyard gain \"You may cast this card from your graveyard\" until end of turn."
        ).withActivationCondition(new SourceIsSolved(), "Activate only if this Case is solved."));
    }
}
