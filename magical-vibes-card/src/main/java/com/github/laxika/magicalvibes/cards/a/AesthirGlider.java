package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "DOM", collectorNumber = "209")
@CardRegistration(set = "ALL", collectorNumber = "116a")
@CardRegistration(set = "ALL", collectorNumber = "116b")
@CardRegistration(set = "ATH", collectorNumber = "67")
public class AesthirGlider extends Card {

    public AesthirGlider() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
