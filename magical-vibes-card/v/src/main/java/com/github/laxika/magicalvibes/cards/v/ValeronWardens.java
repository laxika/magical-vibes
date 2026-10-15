package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

@CardRegistration(set = "ORI", collectorNumber = "203")
public class ValeronWardens extends Card {

    public ValeronWardens() {
        // Renown 2
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(2));

        // Whenever a creature you control becomes renowned, draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_RENOWNED, new DrawCardEffect(1));
    }
}
