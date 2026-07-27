package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;


@CardRegistration(set = "CON", collectorNumber = "46")
public class GrixisSlavedriver extends Card {

    public GrixisSlavedriver() {
        // When this creature leaves the battlefield, create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, CreateTokenEffect.blackZombie(1));

        // Unearth {3}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addUnearth("{3}{B}");
    }
}
