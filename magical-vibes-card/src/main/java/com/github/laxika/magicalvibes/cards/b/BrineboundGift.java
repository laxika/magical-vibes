package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/** Back face of {@link BrineComber}. */
public class BrineboundGift extends Card {

    public BrineboundGift() {
        target(TargetFilters.creature());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(1));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_AURA_SPELL, CreateTokenEffect.whiteSpirit(1));
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
