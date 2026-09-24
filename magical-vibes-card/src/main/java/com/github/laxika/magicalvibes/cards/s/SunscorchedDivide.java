package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "410")
public class SunscorchedDivide extends Card {

    public SunscorchedDivide() {
        // {1}, {T}: Add {R}{W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.RED), new AwardManaEffect(ManaColor.WHITE)),
                "{1}, {T}: Add {R}{W}."
        ));
    }
}
