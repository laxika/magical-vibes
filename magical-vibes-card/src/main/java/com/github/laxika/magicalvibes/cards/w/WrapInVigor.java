package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;

@CardRegistration(set = "FUT", collectorNumber = "141")
public class WrapInVigor extends Card {

    public WrapInVigor() {
        addEffect(EffectSlot.SPELL, new RegenerateAllOwnCreaturesEffect());
    }
}
