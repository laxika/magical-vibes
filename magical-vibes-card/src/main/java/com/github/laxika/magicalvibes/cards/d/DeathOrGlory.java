package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DeathOrGloryEffect;

@CardRegistration(set = "INV", collectorNumber = "13")
public class DeathOrGlory extends Card {

    public DeathOrGlory() {
        addEffect(EffectSlot.SPELL, new DeathOrGloryEffect());
    }
}
