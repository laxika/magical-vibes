package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "110")
public class TrostanisSummoner extends Card {

    public TrostanisSummoner() {
        // When this creature enters, create a 2/2 white Knight creature token with vigilance,
        // a 3/3 green Centaur creature token, and a 4/4 green Rhino creature token with trample.
        // One trigger, three effects in the same slot, resolved in printed order.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect("Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect("Centaur", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.CENTAUR), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect("Rhino", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.RHINO), Set.of(Keyword.TRAMPLE), Set.of()));
    }
}
