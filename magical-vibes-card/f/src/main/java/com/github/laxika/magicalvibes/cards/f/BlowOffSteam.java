package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

public class BlowOffSteam extends Card {

    public BlowOffSteam() {
        addSpellEffect(DealDamageEffect.toAnyTarget(1));
    }
}
