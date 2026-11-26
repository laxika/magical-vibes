package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "88")
public class VeiledShade extends Card {

    public VeiledShade() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{B}", List.of(BoostEffect.self(1, 1)),
                "{1}{B}: Veiled Shade gets +1/+1 until end of turn."));
    }
}
