package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "142")
public class FeralRidgewolf extends Card {

    public FeralRidgewolf() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(BoostEffect.self(2, 0)), "{1}{R}: Feral Ridgewolf gets +2/+0 until end of turn."));
    }
}
