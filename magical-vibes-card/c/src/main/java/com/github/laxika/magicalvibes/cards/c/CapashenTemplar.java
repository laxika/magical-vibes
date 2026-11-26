package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "5")
public class CapashenTemplar extends Card {

    public CapashenTemplar() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(BoostEffect.self(0, 1)), "{W}: Capashen Templar gets +0/+1 until end of turn."));
    }
}
