package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "ONS", collectorNumber = "138")
@CardRegistration(set = "PHUK", collectorNumber = "18")
@CardRegistration(set = "A25", collectorNumber = "86")
@CardRegistration(set = "C13", collectorNumber = "73")
public class DirgeOfDread extends Card {

    public DirgeOfDread() {
        addSpellEffect(new GrantKeywordEffect(Keyword.FEAR, GrantScope.ALL_CREATURES));

        addCycling("{1}{B}");
        target(TargetFilters.creature()).addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET),
                "Have target creature gain fear until end of turn?"));
    }
}
