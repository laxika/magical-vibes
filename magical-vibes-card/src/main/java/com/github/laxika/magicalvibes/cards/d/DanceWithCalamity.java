package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DanceWithCalamityEffect;

@CardRegistration(set = "SOC", collectorNumber = "243")
@CardRegistration(set = "MOC", collectorNumber = "29")
@CardRegistration(set = "MOC", collectorNumber = "116")
public class DanceWithCalamity extends Card {

    public DanceWithCalamity() {
        addEffect(EffectSlot.SPELL, new DanceWithCalamityEffect());
    }
}
