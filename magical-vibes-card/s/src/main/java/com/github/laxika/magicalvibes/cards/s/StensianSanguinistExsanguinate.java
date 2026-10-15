package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.Exsanguinate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Stensian Sanguinist // Exsanguinate (SOC 29).
 */
@CardRegistration(set = "SOC", collectorNumber = "29")
@CardRegistration(set = "SOC", collectorNumber = "78")
public class StensianSanguinistExsanguinate extends Card {

    public StensianSanguinistExsanguinate() {
        setBackFaceCard(new Exsanguinate());

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET))
                .addEffect(EffectSlot.ON_ATTACK,
                        new GrantEffectToTargetUntilEndOfCombatEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "Exsanguinate";
    }
}
