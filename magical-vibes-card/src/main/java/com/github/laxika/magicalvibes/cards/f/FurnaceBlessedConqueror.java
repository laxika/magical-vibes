package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

public class FurnaceBlessedConqueror extends Card {

    public FurnaceBlessedConqueror() {
        addEffect(EffectSlot.ON_ATTACK, CreateTokenCopyOfSourceEffect.tappedAndAttackingWithSourceCounters());
        addEffect(EffectSlot.ON_ATTACK, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
