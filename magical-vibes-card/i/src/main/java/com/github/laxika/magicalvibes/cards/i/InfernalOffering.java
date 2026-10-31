package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.InfernalOfferingEffect;

@CardRegistration(set = "C14", collectorNumber = "24")
@CardRegistration(set = "C21", collectorNumber = "146")
public class InfernalOffering extends Card {

    public InfernalOffering() {
        addSpellEffect(new InfernalOfferingEffect(true));
        addSpellEffect(new InfernalOfferingEffect(false));
    }
}
