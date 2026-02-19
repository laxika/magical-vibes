package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TappedTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "128")
public class Assassinate extends Card {

    public Assassinate() {
        setNeedsTarget(true);
        setTargetFilter(new TappedTargetFilter());
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.CREATURE)));
    }
}
