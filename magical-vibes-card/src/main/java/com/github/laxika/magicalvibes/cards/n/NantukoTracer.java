package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "JUD", collectorNumber = "125")
public class NantukoTracer extends Card {

    public NantukoTracer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .targetGraveyard(true)
                .upTo(true)
                .build());
    }
}
