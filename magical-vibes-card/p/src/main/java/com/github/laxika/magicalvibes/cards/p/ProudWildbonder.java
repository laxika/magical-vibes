package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "IKO", collectorNumber = "229")
public class ProudWildbonder extends Card {

    public ProudWildbonder() {
        addStaticEffect(new GrantEffectEffect(
                new AssignCombatDamageAsThoughUnblockedEffect(),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.TRAMPLE)));
    }
}
