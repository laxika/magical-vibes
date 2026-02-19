package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "196")
public class Demolish extends Card {

    public Demolish() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.ARTIFACT, CardType.LAND)));
    }
}
