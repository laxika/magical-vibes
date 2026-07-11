package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "116")
public class ChameleonColossus extends Card {

    public ChameleonColossus() {
        // Changeling and protection from black are auto-loaded keywords from Scryfall.
        // {2}{G}{G}: This creature gets +X/+X until end of turn, where X is its power.
        // SourcePower snapshots the effective power at resolution, so repeated activations double it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{G}",
                List.of(new BoostSelfEffect(new SourcePower(), new SourcePower())),
                "{2}{G}{G}: This creature gets +X/+X until end of turn, where X is its power."
        ));
    }
}
