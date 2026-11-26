package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "41")
public class SoltariCrusader extends Card {

    public SoltariCrusader() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}", List.of(BoostEffect.self(1, 0)), "{1}{W}: This creature gets +1/+0 until end of turn."));
    }
}
