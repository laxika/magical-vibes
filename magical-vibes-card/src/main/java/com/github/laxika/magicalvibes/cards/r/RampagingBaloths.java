package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "178")
public class RampagingBaloths extends Card {

    public RampagingBaloths() {
        // Landfall — Whenever a land you control enters, create a 4/4 green Beast creature token.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Beast", 4, 4,
                        CardColor.GREEN, List.of(CardSubtype.BEAST),
                        Set.of(), Set.of()));
    }
}
