package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "HOC", collectorNumber = "164")
public class EaglesOfTheNorth extends Card {

    public EaglesOfTheNorth() {
        // When this creature enters, creatures you control get +1/+0 and gain first strike until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostAllOwnCreaturesEffect(1, 0));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES));
    }
}
