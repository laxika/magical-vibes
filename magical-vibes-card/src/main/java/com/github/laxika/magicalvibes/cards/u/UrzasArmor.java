package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "318")
public class UrzasArmor extends Card {

    public UrzasArmor() {
        // "If a source would deal damage to you, prevent 1 of that damage."
        addEffect(EffectSlot.STATIC, new PreventFixedDamagePerSourceToControllerEffect(1));
    }
}
