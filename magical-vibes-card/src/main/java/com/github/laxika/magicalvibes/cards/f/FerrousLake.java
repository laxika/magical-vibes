package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "148")
@CardRegistration(set = "PIP", collectorNumber = "440")
@CardRegistration(set = "PIP", collectorNumber = "676")
@CardRegistration(set = "PIP", collectorNumber = "968")
@CardRegistration(set = "SOC", collectorNumber = "370")
@CardRegistration(set = "TDC", collectorNumber = "361")
@CardRegistration(set = "OTC", collectorNumber = "294")
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
