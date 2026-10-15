package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "9")
@CardRegistration(set = "MPS", collectorNumber = "1")
@CardRegistration(set = "KLR", collectorNumber = "12")
@CardRegistration(set = "MOC", collectorNumber = "176")
@CardRegistration(set = "AFC", collectorNumber = "65")
@CardRegistration(set = "C20", collectorNumber = "80")
@CardRegistration(set = "NEC", collectorNumber = "81")
public class CataclysmicGearhulk extends Card {

    public CataclysmicGearhulk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect(
                List.of(CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.PLANESWALKER), false, true));
    }
}
