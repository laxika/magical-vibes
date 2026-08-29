package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "141")
public class RecklessAmplimancer extends Card {

    public RecklessAmplimancer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(new DoubleSelfPowerToughnessEffect()),
                "{4}{G}: Double this creature's power and toughness until end of turn."
        ));
    }
}
