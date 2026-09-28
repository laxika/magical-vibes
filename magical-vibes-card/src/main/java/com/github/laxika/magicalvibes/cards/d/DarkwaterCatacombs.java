package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "319")
@CardRegistration(set = "PIP", collectorNumber = "260")
@CardRegistration(set = "PIP", collectorNumber = "492")
@CardRegistration(set = "PIP", collectorNumber = "788")
@CardRegistration(set = "PIP", collectorNumber = "1020")
@CardRegistration(set = "40K", collectorNumber = "274")
@CardRegistration(set = "TDC", collectorNumber = "355")
@CardRegistration(set = "OTC", collectorNumber = "282")
@CardRegistration(set = "C20", collectorNumber = "265")
public class DarkwaterCatacombs extends Card {

    public DarkwaterCatacombs() {
        // {1}, {T}: Add {U}{B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLUE), new AwardManaEffect(ManaColor.BLACK)),
                "{1}, {T}: Add {U}{B}."
        ));
    }
}
