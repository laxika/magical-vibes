package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ReturnChosenPermanentsToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOK", collectorNumber = "4")
public class ChargeAcrossTheAraba extends Card {

    public ChargeAcrossTheAraba() {
        // Sweep — Return any number of Plains you control to their owner's hand.
        addSpellEffect(ReturnChosenPermanentsToHandEffect.anyNumber(
                        Subjects.controlledBy(Subjects.you(), new PermanentHasSubtypePredicate(CardSubtype.PLAINS)), "Plains permanent"));

        // Creatures you control get +1/+1 until end of turn for each Plains returned this way.
        addSpellEffect(BoostEffect.creaturesYouControl(new AmountThisWay(StepKind.RETURN), new AmountThisWay(StepKind.RETURN)));
    }
}
