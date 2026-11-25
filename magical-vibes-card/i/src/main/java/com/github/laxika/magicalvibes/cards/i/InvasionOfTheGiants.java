package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "215")
public class InvasionOfTheGiants extends Card {

    public InvasionOfTheGiants() {
        addTrigger(Triggers.chapter(1), new ScryEffect(2));
        addTrigger(Triggers.chapter(2), new DrawCardEffect(1), new MayEffect(
                SequenceEffect.of(
                        ChooseCardEffect.oneOf(Subjects.cardsInYourHand(new CardSubtypePredicate(CardSubtype.GIANT)),
                                "a Giant card to reveal from your hand"),
                        RevealCardsFromHandEffect.cards(Subjects.you(), Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(new HappenedThisWay(StepKind.REVEAL), new QueueReflexiveAbilityEffect(
                                DealDamageEffect.toTargetOpponentOrPlaneswalker(2)))),
                "Reveal a Giant card from your hand to deal 2 damage?"));
        addTrigger(Triggers.chapter(3),
                new ReduceCastCostForNextMatchingSpellEffect(new CardSubtypePredicate(CardSubtype.GIANT), 2));
    }
}
