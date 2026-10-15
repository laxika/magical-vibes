package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "143")
public class MinotaurSureshot extends Card {

    public MinotaurSureshot() {
        // Reach is auto-loaded from Scryfall.
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(1, 0)), "{1}{R}: This creature gets +1/+0 until end of turn."));
    }
}
