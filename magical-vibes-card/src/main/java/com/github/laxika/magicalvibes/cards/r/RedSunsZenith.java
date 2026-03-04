package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "74")
public class RedSunsZenith extends Card {

    public RedSunsZenith() {
        addEffect(EffectSlot.SPELL, new DealXDamageToAnyTargetEffect(true));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
