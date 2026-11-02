package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "63")
public class ArrogantPoet extends Card {

    public ArrogantPoet() {
        addTrigger(Triggers.attacks(), new MayPayLifeEffect(
                2,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                "Pay 2 life to give Arrogant Poet flying until end of turn?"));
    }
}
