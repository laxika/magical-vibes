package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "51")
public class TempestOfLight extends Card {

    public TempestOfLight() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(Set.of(CardType.ENCHANTMENT)));
    }
}
