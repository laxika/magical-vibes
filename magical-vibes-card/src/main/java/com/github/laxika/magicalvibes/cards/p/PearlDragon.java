package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "34")
public class PearlDragon extends Card {

    public PearlDragon() {
        // Flying is auto-loaded from Scryfall keywords.
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}", List.of(new BoostSelfEffect(0, 1)), "{1}{W}: This creature gets +0/+1 until end of turn."));
    }
}
