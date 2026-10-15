package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "MMQ", collectorNumber = "48")
public class SoothingBalm extends Card {

    public SoothingBalm() {
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(5));
    }
}
