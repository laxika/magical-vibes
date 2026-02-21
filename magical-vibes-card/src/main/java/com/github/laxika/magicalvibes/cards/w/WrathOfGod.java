package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "61")
public class WrathOfGod extends Card {

    public WrathOfGod() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(Set.of(CardType.CREATURE), true));
    }
}
