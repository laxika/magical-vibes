package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "3")
public class Cataclysm extends Card {

    public Cataclysm() {
        addEffect(EffectSlot.SPELL, new ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect(
                List.of(CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.LAND), true, true));
    }
}
