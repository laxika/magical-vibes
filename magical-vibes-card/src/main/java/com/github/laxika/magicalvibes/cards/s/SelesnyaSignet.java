package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "270")
@CardRegistration(set = "MM3", collectorNumber = "226")
@CardRegistration(set = "GK1", collectorNumber = "123")
@CardRegistration(set = "AA1", collectorNumber = "11")
@CardRegistration(set = "RVR", collectorNumber = "267")
@CardRegistration(set = "C13", collectorNumber = "257")
@CardRegistration(set = "CMD", collectorNumber = "258")
public class SelesnyaSignet extends Card {

    public SelesnyaSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.GREEN), new AwardManaEffect(ManaColor.WHITE)),
                "{1}, {T}: Add {G}{W}."
        ));
    }
}
