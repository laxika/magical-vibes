package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "278")
@CardRegistration(set = "SIR", collectorNumber = "273")
@CardRegistration(set = "WHO", collectorNumber = "294")
@CardRegistration(set = "WHO", collectorNumber = "507")
@CardRegistration(set = "WHO", collectorNumber = "885")
@CardRegistration(set = "WHO", collectorNumber = "1098")
@CardRegistration(set = "40K", collectorNumber = "289")
@CardRegistration(set = "LTC", collectorNumber = "323")
@CardRegistration(set = "MSC", collectorNumber = "256")
@CardRegistration(set = "MSC", collectorNumber = "484")
@CardRegistration(set = "M3C", collectorNumber = "364")
@CardRegistration(set = "AFC", collectorNumber = "255")
@CardRegistration(set = "NEC", collectorNumber = "174")
@CardRegistration(set = "FIC", collectorNumber = "412")
@CardRegistration(set = "VOC", collectorNumber = "178")
@CardRegistration(set = "SCD", collectorNumber = "313")
public class PortTown extends Card {

    public PortTown() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.PLAINS, CardSubtype.ISLAND)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
