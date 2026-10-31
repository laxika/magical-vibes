package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

/** Flipped face of {@link HomuraHumanAscendant}. */
public class HomurasEssence extends Card {

    public HomurasEssence() {
        addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{R}",
                        List.of(new BoostSelfEffect(1, 0)),
                        "{R}: This creature gets +1/+0 until end of turn."
                ),
                GrantScope.ALL_OWN_CREATURES
        ));
    }
}
