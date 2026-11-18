package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

@CardRegistration(set = "SNC", collectorNumber = "156")
public class RiveteersDecoy extends Card {

    public RiveteersDecoy() {
        addStaticEffect(new MustBeBlockedIfAbleEffect());

        addCastingOption(AlternateHandCast.blitz("{3}{G}"));
    }
}
