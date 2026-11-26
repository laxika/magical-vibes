package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

public class FangbladeEviscerator extends Card {

    public FangbladeEviscerator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        BoostEffect.self(1, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.source())
                ),
                "{1}{R}: This creature gets +1/+0 and gains first strike until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(BoostEffect.creaturesYouControl(2, 0)),
                "{4}{R}: Creatures you control get +2/+0 until end of turn."
        ));
    }
}
