package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "13")
@CardRegistration(set = "ANB", collectorNumber = "8")
public class GoringCeratops extends Card {

    public GoringCeratops() {
        // Whenever Goring Ceratops attacks, other creatures you control gain double strike until end of turn.
        addTrigger(Triggers.attacks(), new GrantKeywordEffect(
                Keyword.DOUBLE_STRIKE, GrantScope.OWN_CREATURES
        ));
    }
}
