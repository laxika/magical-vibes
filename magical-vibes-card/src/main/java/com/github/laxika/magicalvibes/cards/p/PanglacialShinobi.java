package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryNinjutsuEffect;

@CardRegistration(set = "MB2", collectorNumber = "298")
@CardRegistration(set = "MB2", collectorNumber = "534")
public class PanglacialShinobi extends Card {

    public PanglacialShinobi() {
        addEffect(EffectSlot.STATIC, new LibraryNinjutsuEffect("{1}{U}"));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
    }
}
