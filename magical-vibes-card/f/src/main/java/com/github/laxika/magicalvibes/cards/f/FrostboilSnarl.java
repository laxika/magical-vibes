package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "265")
@CardRegistration(set = "WHO", collectorNumber = "282")
@CardRegistration(set = "WHO", collectorNumber = "498")
@CardRegistration(set = "WHO", collectorNumber = "873")
@CardRegistration(set = "WHO", collectorNumber = "1089")
@CardRegistration(set = "LTC", collectorNumber = "312")
@CardRegistration(set = "SOC", collectorNumber = "374")
@CardRegistration(set = "MSC", collectorNumber = "246")
@CardRegistration(set = "MSC", collectorNumber = "475")
@CardRegistration(set = "M3C", collectorNumber = "344")
@CardRegistration(set = "OTC", collectorNumber = "298")
@CardRegistration(set = "LCC", collectorNumber = "332")
@CardRegistration(set = "DRC", collectorNumber = "158")
public class FrostboilSnarl extends Card {

    public FrostboilSnarl() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.ISLAND, CardSubtype.MOUNTAIN)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
