package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DOM", collectorNumber = "147")
public class TwoHeadedGiant extends Card {

    public TwoHeadedGiant() {
        addEffect(EffectSlot.ON_ATTACK, new FlipTwoCoinsEffect(
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
        ));
    }
}
