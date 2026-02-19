package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "14")
public class Demystify extends Card {

    public Demystify() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(Set.of(CardType.ENCHANTMENT)));
    }
}
