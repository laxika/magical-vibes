package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

public class Juggernaut extends Card {

    public Juggernaut() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
        addEffect(EffectSlot.STATIC, new CantBeBlockedBySubtypeEffect(CardSubtype.WALL));
    }
}
