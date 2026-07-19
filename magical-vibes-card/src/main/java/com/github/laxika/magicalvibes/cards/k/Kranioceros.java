package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "67")
public class Kranioceros extends Card {

    public Kranioceros() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}", List.of(new BoostSelfEffect(0, 3)), "{1}{W}: This creature gets +0/+3 until end of turn."));
    }
}
