package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

/** Flipped face of {@link HomuraHumanAscendant}. */
public class HomurasEssence extends Card {

    public HomurasEssence() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.creaturesYouControl()));
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.creaturesYouControl()));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{R}",
                        List.of(BoostEffect.self(1, 0)),
                        "{R}: This creature gets +1/+0 until end of turn."
                ),
                Subjects.creaturesYouControl()
        ));
    }
}
