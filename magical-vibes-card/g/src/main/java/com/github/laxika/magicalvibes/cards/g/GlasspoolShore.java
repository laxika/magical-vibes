package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

public class GlasspoolShore extends Card {

    public GlasspoolShore() {
        addStaticEffect(new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
