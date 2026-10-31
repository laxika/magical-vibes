package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

public class BlowOffSteam extends Card {

    public BlowOffSteam() {
        addSpellEffect(new DealDamageToAnyTargetEffect(1));
    }
}
