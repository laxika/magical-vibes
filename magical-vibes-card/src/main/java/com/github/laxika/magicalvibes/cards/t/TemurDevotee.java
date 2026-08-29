package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "61")
public class TemurDevotee extends Card {

    public TemurDevotee() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED))),
                "{1}: Add {G}, {U}, or {R}. Activate only once each turn.",
                1
        ));
    }
}
