package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.StormChargedSlasher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "157")
@CardRegistration(set = "DBL", collectorNumber = "157")
public class RecklessStormseeker extends Card {

    public RecklessStormseeker() {
        setBackFaceCard(new StormChargedSlasher());

        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new BoostTargetCreatureEffect(1, 0))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
    }

    @Override
    public String getBackFaceClassName() {
        return "StormChargedSlasher";
    }
}
