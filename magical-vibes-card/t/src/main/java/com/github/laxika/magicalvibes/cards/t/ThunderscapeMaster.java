package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "175")
public class ThunderscapeMaster extends Card {

    public ThunderscapeMaster() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}{B}",
                List.of(
                        new LoseLifeEffect(2, Subjects.targetPlayer()),
                        new GainLifeEffect(2)),
                "{B}{B}, {T}: Target player loses 2 life and you gain 2 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{G}",
                List.of(BoostEffect.creaturesYouControl(2, 2)),
                "{G}{G}, {T}: Creatures you control get +2/+2 until end of turn."
        ));
    }
}
