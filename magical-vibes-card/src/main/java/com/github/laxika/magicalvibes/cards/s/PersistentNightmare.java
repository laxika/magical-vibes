package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

public class PersistentNightmare extends Card {

    public PersistentNightmare() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, ReturnToHandEffect.self());
    }
}
