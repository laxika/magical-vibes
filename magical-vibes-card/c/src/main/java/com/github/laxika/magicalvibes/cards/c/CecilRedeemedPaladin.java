package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class CecilRedeemedPaladin extends Card {

    public CecilRedeemedPaladin() {
        addTrigger(Triggers.attacks(), new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES,
                new PermanentIsAttackingPredicate()));
    }
}
