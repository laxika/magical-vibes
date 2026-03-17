package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "239")
public class GavonyTownship extends Card {

    public GavonyTownship() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {2}{G}{W}, {T}: Put a +1/+1 counter on each creature you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}{W}",
                List.of(new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect()),
                "{2}{G}{W}, {T}: Put a +1/+1 counter on each creature you control."
        ));
    }
}
