package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "43")
public class AbyssalNocturnus extends Card {

    public AbyssalNocturnus() {
        addTrigger(Triggers.opponentDiscardsCard(),
                SequenceEffect.of(
                        new BoostSelfEffect(2, 2),
                        new GrantKeywordEffect(Keyword.FEAR, GrantScope.SELF)));
    }
}
