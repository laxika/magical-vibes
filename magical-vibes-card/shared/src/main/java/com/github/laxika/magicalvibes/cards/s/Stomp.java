package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

public class Stomp extends Card {

    public Stomp() {
        addSpellEffect(new DamageCantBePreventedThisTurnEffect());
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
    }
}
