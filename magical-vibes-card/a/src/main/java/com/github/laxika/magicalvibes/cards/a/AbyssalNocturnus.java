package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "GPT", collectorNumber = "43")
public class AbyssalNocturnus extends Card {

    public AbyssalNocturnus() {
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS,
                SequenceEffect.of(
                        new BoostSelfEffect(2, 2),
                        new GrantKeywordEffect(Keyword.FEAR, GrantScope.SELF)));
    }
}
