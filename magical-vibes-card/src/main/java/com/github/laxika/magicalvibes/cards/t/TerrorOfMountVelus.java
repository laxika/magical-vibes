package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "FDN", collectorNumber = "549")
public class TerrorOfMountVelus extends Card {

    public TerrorOfMountVelus() {
        // When this creature enters, creatures you control gain double strike until end of turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.ALL_OWN_CREATURES));
    }
}
