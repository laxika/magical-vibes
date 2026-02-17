package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.Set;

public class Demolish extends Card {

    public Demolish() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.ARTIFACT, CardType.LAND)));
    }
}
