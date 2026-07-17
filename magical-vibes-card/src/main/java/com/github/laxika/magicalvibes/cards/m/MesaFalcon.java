package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "31")
@CardRegistration(set = "5ED", collectorNumber = "46")
public class MesaFalcon extends Card {

    public MesaFalcon() {
        // Flying comes from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}", List.of(new BoostSelfEffect(0, 1)), "{1}{W}: This creature gets +0/+1 until end of turn."));
    }
}
