package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "78")
public class TritonWaverider extends Card {

    public TritonWaverider() {
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
