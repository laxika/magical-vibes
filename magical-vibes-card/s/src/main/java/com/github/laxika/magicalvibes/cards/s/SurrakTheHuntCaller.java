package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "210")
@CardRegistration(set = "CMM", collectorNumber = "326")
@CardRegistration(set = "FDC", collectorNumber = "232")
public class SurrakTheHuntCaller extends Card {

    public SurrakTheHuntCaller() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        TriggerOptions.DEFAULT.withInterveningIf(new ControlledCreaturesTotalPowerAtLeast(8)),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
