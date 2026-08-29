package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

public class TailTheSuspect extends Card {

    public TailTheSuspect() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(1));
    }
}
