package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachedPermanentRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeSuspectedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UnsuspectEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "149")
public class AirtightAlibi extends Card {

    public AirtightAlibi() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new UntapPermanentsEffect(TapUntapScope.ENCHANTED),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.ENCHANTED_CREATURE),
                        new UnsuspectEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new AttachedPermanentRestrictionEffect(new CantBecomeSuspectedEffect()));
    }
}
