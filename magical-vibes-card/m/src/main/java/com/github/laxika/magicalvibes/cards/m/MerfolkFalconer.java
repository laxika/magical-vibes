package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "69")
public class MerfolkFalconer extends Card {

    public MerfolkFalconer() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new KickedSpellCastTriggerEffect(
                List.of(new ScryEffect(2))));
    }
}
