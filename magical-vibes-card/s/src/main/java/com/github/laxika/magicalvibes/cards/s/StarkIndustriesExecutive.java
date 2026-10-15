package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "153")
public class StarkIndustriesExecutive extends Card {

    public StarkIndustriesExecutive() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{2}, {T}: Create a Treasure token."
        ));
    }
}
