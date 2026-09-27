package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "SOC", collectorNumber = "271")
public class GuardianAugmenter extends Card {

    public GuardianAugmenter() {
        var commanderFilter = new PermanentIsCommanderPredicate();
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ALL_OWN_CREATURES, commanderFilter));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HEXPROOF, GrantScope.ALL_OWN_CREATURES, commanderFilter));
    }
}
