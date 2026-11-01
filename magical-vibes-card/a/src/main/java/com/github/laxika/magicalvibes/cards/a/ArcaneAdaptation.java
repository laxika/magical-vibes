package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;

@CardRegistration(set = "XLN", collectorNumber = "46")
public class ArcaneAdaptation extends Card {

    public ArcaneAdaptation() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new GrantChosenSubtypeToOwnCreaturesEffect(true));
    }
}
