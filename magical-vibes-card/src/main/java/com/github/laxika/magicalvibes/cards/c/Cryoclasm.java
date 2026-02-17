package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.filter.LandSubtypeTargetFilter;

import java.util.Set;

public class Cryoclasm extends Card {

    public Cryoclasm() {
        setNeedsTarget(true);
        setTargetFilter(new LandSubtypeTargetFilter(Set.of(CardSubtype.PLAINS, CardSubtype.ISLAND)));
        addEffect(EffectSlot.SPELL, new DestroyTargetLandAndDamageControllerEffect(3));
    }
}
