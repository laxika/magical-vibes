package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentsInsteadOfGraveyardEffect;

@CardRegistration(set = "CHK", collectorNumber = "43")
public class SamuraiOfThePaleCurtain extends Card {

    public SamuraiOfThePaleCurtain() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));
        addEffect(EffectSlot.STATIC, new ExilePermanentsInsteadOfGraveyardEffect());
    }
}
