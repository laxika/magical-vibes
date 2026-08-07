package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

@CardRegistration(set = "ORI", collectorNumber = "41")
public class WarOracle extends Card {

    public WarOracle() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));
    }
}
