package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;

public class EmeriaShatteredSkyclave extends Card {

    public EmeriaShatteredSkyclave() {
        addStaticEffect(new MayPayLifeOrEntersTappedEffect(3));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
