package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerLifeAtMost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "360")
public class YouCannotHideFromMe extends Card {

    public YouCannotHideFromMe() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new BoostTargetCreatureEffect(2, 2),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET),
                        new MakeCreatureUnblockableEffect());
        addTrigger(Triggers.endStep(ActivePlayer.OPPONENT),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TargetPlayerLifeAtMost(GameData.STARTING_LIFE_TOTAL / 2 - 1)),
                new SacrificeSelfEffect());
    }
}
