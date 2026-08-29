package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "KHM", collectorNumber = "38")
public class WarhornBlast extends Card {

    public WarhornBlast() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 1));
        addCastingOption(new ForetellCast("{2}{W}"));
    }
}
