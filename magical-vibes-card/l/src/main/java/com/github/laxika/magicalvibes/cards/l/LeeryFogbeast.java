package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "ONS", collectorNumber = "273")
public class LeeryFogbeast extends Card {

    public LeeryFogbeast() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, PreventDamageEffect.allCombat());
    }
}
