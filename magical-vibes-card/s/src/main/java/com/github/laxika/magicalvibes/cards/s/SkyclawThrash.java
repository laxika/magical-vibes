package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ARB", collectorNumber = "89")
public class SkyclawThrash extends Card {

    public SkyclawThrash() {
        // Whenever Skyclaw Thrash attacks, flip a coin. If you win the flip, it gets
        // +1/+1 and gains flying until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new FlipCoinWinEffect(SequenceEffect.of(
                new BoostSelfEffect(1, 1),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF))));
    }
}
