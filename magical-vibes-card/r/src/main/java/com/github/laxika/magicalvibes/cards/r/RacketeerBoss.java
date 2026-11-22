package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "25")
public class RacketeerBoss extends Card {

    public RacketeerBoss() {
        CreateTokenEffect createTreasure = CreateTokenEffect.ofTreasureToken(1);
        SequenceEffect treasureAbility = SequenceEffect.of(
                createTreasure,
                new RemovePerpetualTriggeredAbilityEffect(Triggers.youCastThisSpell(), SequenceEffect.class));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ChooseCardEffect.upTo(
                        Subjects.cardsInYourHand(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER)))),
                        2, "up to two creature and/or planeswalker cards in your hand"),
                new PerpetuallyGrantAbilitiesEffect(Subjects.cardsThisWay(StepKind.CHOOSE),
                        Triggers.youCastThisSpell(), treasureAbility));
    }
}
