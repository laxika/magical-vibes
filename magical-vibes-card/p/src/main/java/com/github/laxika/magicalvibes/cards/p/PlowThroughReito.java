package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ReturnChosenPermanentsToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOK", collectorNumber = "22")
public class PlowThroughReito extends Card {

    public PlowThroughReito() {
        // Sweep — Return any number of Plains you control to their owner's hand.
        addSpellEffect(ReturnChosenPermanentsToHandEffect.anyNumber(
                        Subjects.controlledBy(Subjects.you(), new PermanentHasSubtypePredicate(CardSubtype.PLAINS)), "Plains permanent"));

        // Target creature gets +1/+1 until end of turn for each Plains returned this way.
        addSpellEffect(new BoostTargetCreatureEffect(new AmountThisWay(StepKind.RETURN), new AmountThisWay(StepKind.RETURN)));
    }
}
