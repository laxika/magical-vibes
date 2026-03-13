package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect;

@CardRegistration(set = "M10", collectorNumber = "98")
@CardRegistration(set = "M11", collectorNumber = "99")
public class HauntingEchoes extends Card {

    public HauntingEchoes() {
        addEffect(EffectSlot.SPELL, new ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect());
    }
}
