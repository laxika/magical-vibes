package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class StormChargedSlasher extends Card {

    public StormChargedSlasher() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new BoostTargetCreatureEffect(2, 0))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }
}
