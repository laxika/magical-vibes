package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerExilesRandomHandCardEffect;

public class RonaTolarianObliterator extends Card {

    public RonaTolarianObliterator() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DamageSourceControllerExilesRandomHandCardEffect());
    }
}
