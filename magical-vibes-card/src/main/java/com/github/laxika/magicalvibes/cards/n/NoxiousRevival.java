package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "NPH", collectorNumber = "118")
public class NoxiousRevival extends Card {

    public NoxiousRevival() {
        addEffect(EffectSlot.SPELL, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY,
                null,
                GraveyardSearchScope.ALL_GRAVEYARDS,
                true,
                false,
                false,
                null,
                false,
                false
        ));
    }
}
