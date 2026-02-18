package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;

public class MidnightRitual extends Card {

    public MidnightRitual() {
        addEffect(EffectSlot.SPELL, new ExileCreaturesFromGraveyardAndCreateTokensEffect());
    }
}
