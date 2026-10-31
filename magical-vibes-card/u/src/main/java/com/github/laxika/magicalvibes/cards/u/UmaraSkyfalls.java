package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

public class UmaraSkyfalls extends Card {

    public UmaraSkyfalls() {
        addStaticEffect(new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
