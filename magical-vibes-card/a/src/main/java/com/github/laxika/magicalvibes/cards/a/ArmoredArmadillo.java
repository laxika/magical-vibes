package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "3")
public class ArmoredArmadillo extends Card {

    public ArmoredArmadillo() {
        // Ward {1}.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(1));

        // {3}{W}: This creature gets +X/+0 until end of turn, where X is its toughness.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new BoostSelfEffect(new SourceToughness(), new Fixed(0))),
                "{3}{W}: This creature gets +X/+0 until end of turn, where X is its toughness."
        ));
    }
}
