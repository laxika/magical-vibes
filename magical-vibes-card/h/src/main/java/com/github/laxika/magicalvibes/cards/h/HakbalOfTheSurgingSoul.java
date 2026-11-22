package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEachControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "3")
@CardRegistration(set = "LCC", collectorNumber = "19")
@CardRegistration(set = "LCC", collectorNumber = "29")
@CardRegistration(set = "LCC", collectorNumber = "123")
public class HakbalOfTheSurgingSoul extends Card {

    public HakbalOfTheSurgingSoul() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new ExploreEachControlledCreatureEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)))));

        CardTypePredicate land = new CardTypePredicate(CardType.LAND);
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        ChooseCardEffect.upTo(Subjects.cardsInYourHand(land), 1,
                                "a land card from your hand to put onto the battlefield"),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(new NotCondition(HappenedThisWay.returned()), new DrawCardEffect())),
                "Put a land card from your hand onto the battlefield?",
                new DrawCardEffect()));
    }
}
