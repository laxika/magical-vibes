package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "152")
@CardRegistration(set = "PIP", collectorNumber = "444")
@CardRegistration(set = "PIP", collectorNumber = "680")
@CardRegistration(set = "PIP", collectorNumber = "972")
@CardRegistration(set = "SOC", collectorNumber = "392")
@CardRegistration(set = "DSC", collectorNumber = "293")
@CardRegistration(set = "BLC", collectorNumber = "321")
public class OverflowingBasin extends Card {

    public OverflowingBasin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.GREEN), new AwardManaEffect(ManaColor.BLUE)),
                "{1}, {T}: Add {G}{U}."
        ));
    }
}
