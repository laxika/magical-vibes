package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "270")
@CardRegistration(set = "SIR", collectorNumber = "264")
@CardRegistration(set = "WHO", collectorNumber = "261")
@CardRegistration(set = "WHO", collectorNumber = "484")
@CardRegistration(set = "WHO", collectorNumber = "852")
@CardRegistration(set = "WHO", collectorNumber = "1075")
@CardRegistration(set = "40K", collectorNumber = "268")
@CardRegistration(set = "LTC", collectorNumber = "299")
@CardRegistration(set = "MSC", collectorNumber = "229")
@CardRegistration(set = "MSC", collectorNumber = "464")
@CardRegistration(set = "MKC", collectorNumber = "254")
@CardRegistration(set = "AFC", collectorNumber = "228")
@CardRegistration(set = "LCC", collectorNumber = "322")
@CardRegistration(set = "MIC", collectorNumber = "169")
@CardRegistration(set = "WOC", collectorNumber = "155")
@CardRegistration(set = "FIC", collectorNumber = "379")
public class ChokedEstuary extends Card {

    public ChokedEstuary() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.ISLAND, CardSubtype.SWAMP)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
