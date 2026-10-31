package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

/** Back face of Mystic Skull. */
public class MysticMonstrosity extends Card {

    public MysticMonstrosity() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(),
                GrantScope.OWN_LANDS
        ));
    }
}
