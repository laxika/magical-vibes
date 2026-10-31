package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

public class Stomp extends Card {

    public Stomp() {
        addSpellEffect(new DamageCantBePreventedThisTurnEffect());
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
    }
}
