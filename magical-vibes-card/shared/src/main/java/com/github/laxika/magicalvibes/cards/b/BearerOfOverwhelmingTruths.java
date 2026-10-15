package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class BearerOfOverwhelmingTruths extends Card {

    public BearerOfOverwhelmingTruths() {
        // Whenever this creature deals combat damage to a player, investigate.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, CreateTokenEffect.ofClueToken(1));
    }
}
