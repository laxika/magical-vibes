package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlipTwoCoinsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "147")
public class TwoHeadedGiant extends Card {

    public TwoHeadedGiant() {
        addTrigger(Triggers.attacks(), new FlipTwoCoinsEffect(
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
        ));
    }
}
