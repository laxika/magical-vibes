package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "152")
@CardRegistration(set = "MM3", collectorNumber = "223")
@CardRegistration(set = "DDJ", collectorNumber = "17")
@CardRegistration(set = "SLD", collectorNumber = "293")
@CardRegistration(set = "GK1", collectorNumber = "46")
@CardRegistration(set = "AA1", collectorNumber = "8")
@CardRegistration(set = "RVR", collectorNumber = "261")
@CardRegistration(set = "CMD", collectorNumber = "252")
@CardRegistration(set = "C15", collectorNumber = "256")
public class IzzetSignet extends Card {

    public IzzetSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLUE), new AwardManaEffect(ManaColor.RED)),
                "{1}, {T}: Add {U}{R}."
        ));
    }
}
