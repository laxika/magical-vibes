package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "234")
public class MerrowSkyswimmer extends Card {

    public MerrowSkyswimmer() {
        // When this creature enters, create a 1/1 white and blue Merfolk creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Merfolk", 1, 1, null,
                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.MERFOLK)));
    }
}
