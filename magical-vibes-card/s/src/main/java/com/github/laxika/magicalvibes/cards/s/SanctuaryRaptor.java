package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "233")
public class SanctuaryRaptor extends Card {

    public SanctuaryRaptor() {
        // Whenever this creature attacks, if you control three or more tokens, it gets +2/+0 and
        // gains first strike until end of turn.
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new ControlsPermanentCount(3, new PermanentIsTokenPredicate()),
                SequenceEffect.of(
                        new BoostSelfEffect(2, 0),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF))));
    }
}
