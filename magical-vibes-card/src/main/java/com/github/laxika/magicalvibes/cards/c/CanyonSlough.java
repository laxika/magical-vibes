package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "AKH", collectorNumber = "239")
@CardRegistration(set = "AKR", collectorNumber = "284")
@CardRegistration(set = "MSC", collectorNumber = "228")
@CardRegistration(set = "MSC", collectorNumber = "463")
@CardRegistration(set = "ECC", collectorNumber = "145")
@CardRegistration(set = "WHO", collectorNumber = "259")
@CardRegistration(set = "WHO", collectorNumber = "482")
@CardRegistration(set = "WHO", collectorNumber = "850")
@CardRegistration(set = "WHO", collectorNumber = "1073")
@CardRegistration(set = "PIP", collectorNumber = "256")
@CardRegistration(set = "PIP", collectorNumber = "489")
@CardRegistration(set = "PIP", collectorNumber = "784")
@CardRegistration(set = "PIP", collectorNumber = "1017")
@CardRegistration(set = "DSC", collectorNumber = "266")
@CardRegistration(set = "TDC", collectorNumber = "344")
@CardRegistration(set = "OTC", collectorNumber = "275")
public class CanyonSlough extends Card {

    public CanyonSlough() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
