package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

public class CecilRedeemedPaladin extends Card {

    public CecilRedeemedPaladin() {
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES,
                new PermanentIsAttackingPredicate()));
    }
}
