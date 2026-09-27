package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "370")
public class FerrousLake extends Card {

    public FerrousLake() {
        // {1}, {T}: Add {U}{R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLUE), new AwardManaEffect(ManaColor.RED)),
                "{1}, {T}: Add {U}{R}."
        ));
    }
}
