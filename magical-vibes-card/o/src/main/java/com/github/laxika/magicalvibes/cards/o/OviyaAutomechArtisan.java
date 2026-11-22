package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "173")
public class OviyaAutomechArtisan extends Card {

    public OviyaAutomechArtisan() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.TRAMPLE, Subjects.allCreatures(),
                new PermanentIsAttackingOpponentOfSourceControllerPredicate()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new MayEffect(
                        SequenceEffect.of(
                                ChooseCardEffect.upTo(Subjects.cardsInYourHand(new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardSubtypePredicate(CardSubtype.VEHICLE)))), 1,
                                        "a creature or Vehicle card from your hand to put onto the battlefield"),
                                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                                new ConditionalEffect(
                                        HappenedThisWay.toA(StepKind.RETURN, new PermanentIsArtifactPredicate()),
                                        new PutCounterOnTargetPermanentEffect(Subjects.returnedThisResolution(),
                                                CounterType.PLUS_ONE_PLUS_ONE, 2))),
                        "Put a creature or Vehicle card from your hand onto the battlefield?"
                )),
                "{G}, {T}: You may put a creature or Vehicle card from your hand onto the battlefield. "
                        + "If you put an artifact onto the battlefield this way, put two +1/+1 counters on it."
        ));
    }
}
