package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LandManaProducesFixedColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "23")
public class DeepWater extends Card {

    public DeepWater() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new LandManaProducesFixedColorUntilEndOfTurnEffect(ManaColor.BLUE)),
                "{U}: Until end of turn, if you tap a land you control for mana, it produces {U} instead of any other type."));
    }
}
