package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "70")
public class FeralAnimist extends Card {

    public FeralAnimist() {
        // {3}: This creature gets +X/+0 until end of turn, where X is its power.
        // SourcePower snapshots the effective power at resolution, so repeated activations double it.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new BoostSelfEffect(new SourcePower(), new Fixed(0))),
                "{3}: This creature gets +X/+0 until end of turn, where X is its power."
        ));
    }
}
