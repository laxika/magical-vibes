package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleWhenControllerControlsExactlyTwoSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

@CardRegistration(set = "YWOE", collectorNumber = "27")
public class SyrJoshuaAndSyrSaxon extends Card {

    private static final String CARD_NAME = "Syr Joshua and Syr Saxon";

    public SyrJoshuaAndSyrSaxon() {
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleWhenControllerControlsExactlyTwoSameNameEffect());
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.BATTLE_CRY,
                GrantScope.OWN_CREATURES,
                new PermanentNamedPredicate(CARD_NAME)));
    }
}
