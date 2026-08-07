package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

@CardRegistration(set = "ORI", collectorNumber = "36")
public class TopanFreeblade extends Card {

    public TopanFreeblade() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));
    }
}
