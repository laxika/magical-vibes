package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;

public class RuleOfLaw extends Card {

    public RuleOfLaw() {
        addEffect(EffectSlot.STATIC, new LimitSpellsPerTurnEffect(1));
    }
}
