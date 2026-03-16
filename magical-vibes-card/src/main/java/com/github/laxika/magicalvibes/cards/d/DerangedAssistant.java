package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "52")
public class DerangedAssistant extends Card {

    public DerangedAssistant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillControllerCost(1), new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}, Mill a card: Add {C}."
        ));
    }
}
