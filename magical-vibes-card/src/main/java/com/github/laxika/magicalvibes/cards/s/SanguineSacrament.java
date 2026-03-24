package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeMultipliedByXValueEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;

@CardRegistration(set = "XLN", collectorNumber = "33")
public class SanguineSacrament extends Card {

    public SanguineSacrament() {
        addEffect(EffectSlot.SPELL, new GainLifeMultipliedByXValueEffect(2));
        addEffect(EffectSlot.SPELL, new PutSelfOnBottomOfOwnersLibraryEffect());
    }
}
