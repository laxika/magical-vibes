package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MasterOfPredicamentsEffect;

@CardRegistration(set = "M15", collectorNumber = "67")
public class MasterOfPredicaments extends Card {

    public MasterOfPredicaments() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MasterOfPredicamentsEffect());
    }
}
