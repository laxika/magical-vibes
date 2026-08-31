package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControllerPlayedAtLeastLandsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalManaEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "179")
public class RiverOfTears extends Card {

    public RiverOfTears() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ConditionalManaEffect(
                        new ControllerPlayedAtLeastLandsThisTurn(1), ManaColor.BLACK, ManaColor.BLUE)),
                "{T}: Add {U}. If you played a land this turn, add {B} instead."
        ));
    }
}
