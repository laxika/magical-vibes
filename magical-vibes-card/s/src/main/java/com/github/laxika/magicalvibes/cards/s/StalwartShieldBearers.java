package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ROE", collectorNumber = "46")
public class StalwartShieldBearers extends Card {

    public StalwartShieldBearers() {
        // Other creatures you control with defender get +0/+2.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.DEFENDER)));
    }
}
