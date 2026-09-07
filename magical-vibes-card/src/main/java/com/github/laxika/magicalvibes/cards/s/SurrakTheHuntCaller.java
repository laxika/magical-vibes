package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "210")
public class SurrakTheHuntCaller extends Card {

    public SurrakTheHuntCaller() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new ConditionalEffect(
                                new ControlledCreaturesTotalPowerAtLeast(8),
                                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET)));
    }
}
