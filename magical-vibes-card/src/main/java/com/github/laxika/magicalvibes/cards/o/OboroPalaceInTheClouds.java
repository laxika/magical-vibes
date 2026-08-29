package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "164")
public class OboroPalaceInTheClouds extends Card {

    public OboroPalaceInTheClouds() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(ReturnToHandEffect.self()),
                "{1}: Return Oboro, Palace in the Clouds to its owner's hand."
        ));
    }
}
