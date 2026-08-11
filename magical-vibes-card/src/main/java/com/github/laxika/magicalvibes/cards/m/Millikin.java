package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "302")
public class Millikin extends Card {

    public Millikin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillControllerCost(1), new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}, Mill a card: Add {C}."
        ));
    }
}
