package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

@CardRegistration(set = "ORI", collectorNumber = "213")
public class CitadelCastellan extends Card {

    public CitadelCastellan() {
        // Renown 2
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(2));
    }
}
