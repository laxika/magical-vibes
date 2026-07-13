package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "POR", collectorNumber = "164")
@CardRegistration(set = "6ED", collectorNumber = "224")
public class ElvenCache extends Card {

    public ElvenCache() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).targetGraveyard(true).build());
    }
}
