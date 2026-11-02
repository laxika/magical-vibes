package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "137")
public class CriticalHit extends Card {

    public CriticalHit() {
        target(TargetFilters.creature())
                .addSpellEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET));
        addTrigger(new TriggerCondition(Triggers.youRollNatural20(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
