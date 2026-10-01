package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "266")
@CardRegistration(set = "WHO", collectorNumber = "283")
@CardRegistration(set = "WHO", collectorNumber = "1090")
@CardRegistration(set = "LTC", collectorNumber = "313")
@CardRegistration(set = "SOC", collectorNumber = "375")
@CardRegistration(set = "MSC", collectorNumber = "247")
@CardRegistration(set = "MSC", collectorNumber = "476")
@CardRegistration(set = "M3C", collectorNumber = "345")
@CardRegistration(set = "MKC", collectorNumber = "263")
@CardRegistration(set = "LCC", collectorNumber = "333")
public class FurycalmSnarl extends Card {

    public FurycalmSnarl() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.MOUNTAIN, CardSubtype.PLAINS)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
