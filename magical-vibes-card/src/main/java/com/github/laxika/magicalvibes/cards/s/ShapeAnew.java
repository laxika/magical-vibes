package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetThenRevealUntilTypeToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "43")
public class ShapeAnew extends Card {

    public ShapeAnew() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new SacrificeTargetThenRevealUntilTypeToBattlefieldEffect(Set.of(CardType.ARTIFACT)));
    }
}
