package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "326")
@CardRegistration(set = "WHO", collectorNumber = "303")
@CardRegistration(set = "WHO", collectorNumber = "894")
@CardRegistration(set = "WHO", collectorNumber = "513")
@CardRegistration(set = "WHO", collectorNumber = "1104")
@CardRegistration(set = "PIP", collectorNumber = "288")
@CardRegistration(set = "PIP", collectorNumber = "507")
@CardRegistration(set = "PIP", collectorNumber = "816")
@CardRegistration(set = "PIP", collectorNumber = "1035")
@CardRegistration(set = "DSC", collectorNumber = "296")
@CardRegistration(set = "AFC", collectorNumber = "259")
@CardRegistration(set = "C20", collectorNumber = "309")
@CardRegistration(set = "FIC", collectorNumber = "421")
public class ShadowbloodRidge extends Card {

    public ShadowbloodRidge() {
        // {1}, {T}: Add {B}{R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new AwardManaEffect(ManaColor.BLACK),
                        new AwardManaEffect(ManaColor.RED)
                ),
                "{1}, {T}: Add {B}{R}."
        ));
    }
}
