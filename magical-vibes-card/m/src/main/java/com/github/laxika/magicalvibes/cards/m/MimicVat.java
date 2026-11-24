package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "175")
@CardRegistration(set = "LCC", collectorNumber = "115")
@CardRegistration(set = "C20", collectorNumber = "246")
@CardRegistration(set = "C19", collectorNumber = "219")
@CardRegistration(set = "C18", collectorNumber = "209")
public class MimicVat extends Card {

    public MimicVat() {
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.ANY),
                new MayEffect(SequenceEffect.of(
                        ExileGraveyardCardsEffect.cards(Subjects.triggeringCards()).imprintingOnSource(),
                        new ConditionalEffect(HappenedThisWay.exiled(), new PutExiledCardsIntoOwnersGraveyardEffect(
                                Subjects.except(Subjects.exiledWithSource(),
                                        Subjects.cardsThisWay(StepKind.EXILE))))),
                        "You may exile that card."));
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new CreateTokenCopyOfImprintedCardEffect()),
                "{3}, {T}: Create a token that's a copy of a card exiled with Mimic Vat. It gains haste. Exile it at the beginning of the next end step."));
    }
}
