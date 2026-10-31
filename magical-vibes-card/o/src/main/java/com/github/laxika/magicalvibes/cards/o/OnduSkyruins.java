package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

public class OnduSkyruins extends Card {

    public OnduSkyruins() {
        addStaticEffect(new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
