package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "GRN", collectorNumber = "25")
public class SkylineScout extends Card {

    public SkylineScout() {
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{1}{W}",
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                "Pay {1}{W} to give Skyline Scout flying until end of turn?"
        ));
    }
}
