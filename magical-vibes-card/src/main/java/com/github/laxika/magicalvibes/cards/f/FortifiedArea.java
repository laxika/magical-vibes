package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Fortified Area — {1}{W}{W} Enchantment.
 * "Wall creatures you control get +1/+0 and have banding."
 */
@CardRegistration(set = "4ED", collectorNumber = "26")
public class FortifiedArea extends Card {

    public FortifiedArea() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.WALL)));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.BANDING, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.WALL)));
    }
}
