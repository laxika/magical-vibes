package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "AKH", collectorNumber = "245")
@CardRegistration(set = "AKR", collectorNumber = "304")
@CardRegistration(set = "WHO", collectorNumber = "288")
@CardRegistration(set = "WHO", collectorNumber = "504")
@CardRegistration(set = "WHO", collectorNumber = "879")
@CardRegistration(set = "WHO", collectorNumber = "1095")
@CardRegistration(set = "PIP", collectorNumber = "268")
@CardRegistration(set = "PIP", collectorNumber = "499")
@CardRegistration(set = "PIP", collectorNumber = "796")
@CardRegistration(set = "PIP", collectorNumber = "1027")
@CardRegistration(set = "SLD", collectorNumber = "2519")
@CardRegistration(set = "MSC", collectorNumber = "251")
@CardRegistration(set = "MSC", collectorNumber = "481")
@CardRegistration(set = "TDC", collectorNumber = "372")
@CardRegistration(set = "MKC", collectorNumber = "267")
@CardRegistration(set = "C20", collectorNumber = "282")
@CardRegistration(set = "DRC", collectorNumber = "161")
public class IrrigatedFarmland extends Card {

    public IrrigatedFarmland() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
