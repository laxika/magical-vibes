package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "74")
public class ThirstForMeaning extends Card {

    public ThirstForMeaning() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL,
                new DiscardTwoUnlessCardTypeEffect(Set.of(CardType.ENCHANTMENT)));
    }
}
