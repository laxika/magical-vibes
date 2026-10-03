package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "274")
@CardRegistration(set = "WHO", collectorNumber = "329")
@CardRegistration(set = "WHO", collectorNumber = "532")
@CardRegistration(set = "WHO", collectorNumber = "920")
@CardRegistration(set = "WHO", collectorNumber = "1123")
@CardRegistration(set = "LTC", collectorNumber = "343")
@CardRegistration(set = "SOC", collectorNumber = "420")
@CardRegistration(set = "DSC", collectorNumber = "323")
@CardRegistration(set = "FIC", collectorNumber = "440")
@CardRegistration(set = "DRC", collectorNumber = "183")
public class VineglimmerSnarl extends Card {

    public VineglimmerSnarl() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.FOREST, CardSubtype.ISLAND)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
