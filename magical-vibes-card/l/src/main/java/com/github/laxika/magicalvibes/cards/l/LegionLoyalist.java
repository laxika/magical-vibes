package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "97")
@CardRegistration(set = "GK1", collectorNumber = "82")
@CardRegistration(set = "PIO", collectorNumber = "144")
public class LegionLoyalist extends Card {

    public LegionLoyalist() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new MinimumAttackers(3)),
                SequenceEffect.of(
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE), GrantScope.ALL_OWN_CREATURES),
                        new GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(
                                null,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                "nontoken creatures")));
    }
}
