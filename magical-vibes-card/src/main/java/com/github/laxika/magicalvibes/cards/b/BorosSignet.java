package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "255")
@CardRegistration(set = "HOP", collectorNumber = "107")
@CardRegistration(set = "MM3", collectorNumber = "217")
@CardRegistration(set = "GK1", collectorNumber = "97")
@CardRegistration(set = "AA1", collectorNumber = "2")
@CardRegistration(set = "RVR", collectorNumber = "251")
public class BorosSignet extends Card {

    public BorosSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.RED), new AwardManaEffect(ManaColor.WHITE)),
                "{1}, {T}: Add {R}{W}."
        ));
    }
}
