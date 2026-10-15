package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

public class LupariShield extends Card {

    public LupariShield() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                GrantDuration.UNTIL_YOUR_NEXT_TURN,
                null));
    }
}
