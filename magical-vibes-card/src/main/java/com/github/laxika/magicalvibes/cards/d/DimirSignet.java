package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "260")
@CardRegistration(set = "MM3", collectorNumber = "219")
@CardRegistration(set = "SLD", collectorNumber = "287")
@CardRegistration(set = "GK1", collectorNumber = "22")
@CardRegistration(set = "AA1", collectorNumber = "5")
@CardRegistration(set = "RVR", collectorNumber = "256")
@CardRegistration(set = "CMD", collectorNumber = "246")
@CardRegistration(set = "MKC", collectorNumber = "226")
@CardRegistration(set = "LCC", collectorNumber = "302")
@CardRegistration(set = "C18", collectorNumber = "203")
public class DimirSignet extends Card {

    public DimirSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLUE), new AwardManaEffect(ManaColor.BLACK)),
                "{1}, {T}: Add {U}{B}."
        ));
    }
}
