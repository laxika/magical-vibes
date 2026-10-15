package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

public class FollowHim extends Card {

    public FollowHim() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(new XValue()));
    }
}
