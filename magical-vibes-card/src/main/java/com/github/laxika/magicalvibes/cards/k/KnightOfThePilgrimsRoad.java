package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

@CardRegistration(set = "ORI", collectorNumber = "20")
public class KnightOfThePilgrimsRoad extends Card {

    public KnightOfThePilgrimsRoad() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));
    }
}
