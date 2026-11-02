package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "23")
@CardRegistration(set = "OM1", collectorNumber = "11")
@CardRegistration(set = "MSC", collectorNumber = "781")
public class WildPackSquad extends Card {

    public WildPackSquad() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new GrantKeywordEffect(
                                Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE),
                                GrantScope.TARGET));
    }
}
