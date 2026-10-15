package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.s.StudentOfElements}.
 */
public class TobitaMasterOfWinds extends Card {

    public TobitaMasterOfWinds() {
        // "Creatures you control have flying." - includes Tobita itself.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_OWN_CREATURES));
    }
}
