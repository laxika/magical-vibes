package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantCastSpellTypeEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "344")
public class SteelGolem extends Card {

    public SteelGolem() {
        addEffect(EffectSlot.STATIC, new CantCastSpellTypeEffect(Set.of(CardType.CREATURE)));
    }
}
