package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "45")
@CardRegistration(set = "NCC", collectorNumber = "146")
@CardRegistration(set = "SOC", collectorNumber = "122")
public class DeterminedIteration extends Card {

    public DeterminedIteration() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new PopulateEffect(),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TOKENS_CREATED_THIS_RESOLUTION,
                        GrantDuration.INDEFINITE),
                new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
