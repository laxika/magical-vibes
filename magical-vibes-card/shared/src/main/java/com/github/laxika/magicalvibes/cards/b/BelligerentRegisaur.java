package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


public class BelligerentRegisaur extends Card {

    public BelligerentRegisaur() {
        addTrigger(Triggers.youCastSpell().anySpell(), new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF));
    }
}
