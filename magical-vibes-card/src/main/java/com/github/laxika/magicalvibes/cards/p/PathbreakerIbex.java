package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SLD", collectorNumber = "307")
@CardRegistration(set = "SPG", collectorNumber = "91")
@CardRegistration(set = "SPG", collectorNumber = "101")
@CardRegistration(set = "C15", collectorNumber = "38")
public class PathbreakerIbex extends Card {

    public PathbreakerIbex() {
        // Whenever this creature attacks, creatures you control gain trample and get +X/+X
        // until end of turn, where X is the greatest power among creatures you control.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(
                new GreatestPowerAmongControlled(), new GreatestPowerAmongControlled()));
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));
    }
}
