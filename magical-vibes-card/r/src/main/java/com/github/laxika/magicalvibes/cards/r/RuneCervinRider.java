package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "20")
public class RuneCervinRider extends Card {

    public RuneCervinRider() {
        // Flying keyword is auto-loaded from Scryfall.
        // {G/W}{G/W}: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{G/W}{G/W}", List.of(new BoostSelfEffect(1, 1)),
                "{G/W}{G/W}: This creature gets +1/+1 until end of turn."));
    }
}
