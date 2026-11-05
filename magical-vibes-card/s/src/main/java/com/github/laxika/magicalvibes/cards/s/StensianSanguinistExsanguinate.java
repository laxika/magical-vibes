package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.Exsanguinate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Stensian Sanguinist // Exsanguinate (SOC 29).
 */
@CardRegistration(set = "SOC", collectorNumber = "29")
@CardRegistration(set = "SOC", collectorNumber = "78")
public class StensianSanguinistExsanguinate extends Card {

    public StensianSanguinistExsanguinate() {
        setBackFaceCard(new Exsanguinate());

        target(TargetFilters.creature())
                .addTrigger(Triggers.youAttack(),
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET),
                        new GrantEffectToTargetUntilEndOfCombatEffect(
                                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                new BecomePreparedEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "Exsanguinate";
    }
}
