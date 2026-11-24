package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "138")
public class ArmoredSkyhunter extends Card {

    public ArmoredSkyhunter() {
        LookAtTopCardsEffect search = LookAtTopCardsEffect
                .mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                        6,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.AURA),
                                new CardSubtypePredicate(CardSubtype.EQUIPMENT))),
                        LibrarySelectionFollowUp.inResolution(new MayEffect(
                                AttachSourceEquipmentToTargetCreatureEffect.pickedEquipmentToCreatureYouControl(),
                                "Attach that Equipment to a creature you control?")));
        addTrigger(Triggers.attacks(), search);
    }
}
