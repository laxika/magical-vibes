package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "76")
@CardRegistration(set = "DDM", collectorNumber = "44")
@CardRegistration(set = "RVR", collectorNumber = "92")
public class ShadowAlleyDenizen extends Card {

    public ShadowAlleyDenizen() {
        // Whenever another black creature you control enters, target creature gains intimidate until end of turn.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(Triggers.CREATURE,
                ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardColorPredicate(CardColor.BLACK),
                        new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.TARGET)));
    }
}
