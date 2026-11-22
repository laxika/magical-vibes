package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "174")
public class ArmoredKincaller extends Card {

    public ArmoredKincaller() {
        PermanentHasSubtypePredicate dinosaur = new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR);
        ControlsAnotherPermanent controlsAnotherDinosaur = new ControlsAnotherPermanent(dinosaur);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(SequenceEffect.of(
                        ChooseCardEffect.oneOf(Subjects.cardsInYourHand(new CardSubtypePredicate(CardSubtype.DINOSAUR)),
                                "a Dinosaur card to reveal from your hand"),
                        RevealCardsFromHandEffect.cards(Subjects.you(), Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(new HappenedThisWay(StepKind.REVEAL), new ConditionalEffect(
                                new NotCondition(controlsAnotherDinosaur), new GainLifeEffect(3)))),
                        "Reveal a Dinosaur card from your hand?"),
                new ConditionalEffect(controlsAnotherDinosaur, new GainLifeEffect(3)));
    }
}
