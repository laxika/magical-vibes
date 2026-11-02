package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerEqualsToughnessPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "138")
public class MasterSymmetrist extends Card {

    public MasterSymmetrist() {
        addTrigger(Triggers.creatureYouControlAttacks(), new TriggeringPermanentConditionalEffect(
                new PermanentPowerEqualsToughnessPredicate(),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));
    }
}
