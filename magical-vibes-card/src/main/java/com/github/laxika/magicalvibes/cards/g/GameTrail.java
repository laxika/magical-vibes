package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "276")
@CardRegistration(set = "SIR", collectorNumber = "269")
@CardRegistration(set = "WHO", collectorNumber = "284")
@CardRegistration(set = "WHO", collectorNumber = "500")
@CardRegistration(set = "WHO", collectorNumber = "875")
@CardRegistration(set = "WHO", collectorNumber = "1091")
@CardRegistration(set = "LCC", collectorNumber = "334")
@CardRegistration(set = "40K", collectorNumber = "282")
@CardRegistration(set = "MKC", collectorNumber = "264")
@CardRegistration(set = "AFC", collectorNumber = "240")
@CardRegistration(set = "BLC", collectorNumber = "306")
@CardRegistration(set = "NEC", collectorNumber = "169")
@CardRegistration(set = "FIC", collectorNumber = "398")
@CardRegistration(set = "SCD", collectorNumber = "302")
public class GameTrail extends Card {

    public GameTrail() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.MOUNTAIN, CardSubtype.FOREST)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
