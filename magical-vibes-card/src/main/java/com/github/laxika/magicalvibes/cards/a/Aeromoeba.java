package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "37")
public class Aeromoeba extends Card {

    public Aeromoeba() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new SwitchPowerToughnessEffect(true)),
                "Discard a card: Switch this creature's power and toughness until end of turn."
        ));
    }
}
