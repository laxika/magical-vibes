package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenBasicLandTypeToOwnLandsEffect;

@CardRegistration(set = "GTC", collectorNumber = "45")
public class Realmwright extends Card {

    public Realmwright() {
        addStaticEffect(new ChooseBasicLandTypeOnEnterEffect());
        addStaticEffect(new GrantChosenBasicLandTypeToOwnLandsEffect());
    }
}
