package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalColorSourceDamageEffect;

@CardRegistration(set = "ORI", collectorNumber = "141")
public class EmbermawHellion extends Card {

    public EmbermawHellion() {
        // If another red source you control would deal damage to a permanent or player,
        // it deals that much damage plus 1 to that permanent or player instead.
        addEffect(EffectSlot.STATIC, new AdditionalColorSourceDamageEffect(1, CardColor.RED));
    }
}
