package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "10E", collectorNumber = "289")
public class Recollect extends Card {

    public Recollect() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).targetGraveyard(true).build());
    }
}
