package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "30")
@CardRegistration(set = "DBL", collectorNumber = "30")
public class RitualGuardian extends Card {

    public RitualGuardian() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Coven()),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF));
    }
}
