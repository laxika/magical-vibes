package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "10E", collectorNumber = "289")
@CardRegistration(set = "M19", collectorNumber = "197")
@CardRegistration(set = "RAV", collectorNumber = "178")
public class Recollect extends Card {

    public Recollect() {
        addSpellEffect(ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).targetGraveyard(true).build());
    }
}
