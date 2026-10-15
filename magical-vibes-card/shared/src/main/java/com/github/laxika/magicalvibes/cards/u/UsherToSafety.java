package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class UsherToSafety extends Card {

    public UsherToSafety() {
        target(TargetFilters.permanentYouControl())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
