package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ReturnChosenPermanentsToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOK", collectorNumber = "94")
public class BarrelDownSokenzan extends Card {

    public BarrelDownSokenzan() {
        // Sweep — Return any number of Mountains you control to their owner's hand.
        target(TargetFilters.creature());
        addSpellEffect(ReturnChosenPermanentsToHandEffect.anyNumber(
                        Subjects.controlledBy(Subjects.you(), new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)), "Mountain"));

        // Barrel Down Sokenzan deals damage to target creature equal to twice the number of
        // Mountains returned this way.
        addSpellEffect(DealDamageEffect.toTargetCreature(new Scaled(new AmountThisWay(StepKind.RETURN), 2)));
    }
}
