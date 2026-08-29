package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "160")
public class SultaiDevotee extends Card {

    public SultaiDevotee() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN, ManaColor.BLUE))),
                "{1}: Add {B}, {G}, or {U}. Activate only once each turn.",
                1
        ));
    }
}
