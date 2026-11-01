package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;

@CardRegistration(set = "NPH", collectorNumber = "51")
public class Xenograft extends Card {

    public Xenograft() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new GrantChosenSubtypeToOwnCreaturesEffect());
    }
}
