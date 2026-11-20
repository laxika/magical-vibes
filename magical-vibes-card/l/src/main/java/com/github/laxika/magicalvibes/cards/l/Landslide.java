package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "UDS", collectorNumber = "92")
public class Landslide extends Card {

    public Landslide() {
        addSpellEffect(SacrificePermanentsEffect.anyNumber(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), Subjects.you()));
        addSpellEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(new AmountThisWay(StepKind.SACRIFICE)));
    }
}
