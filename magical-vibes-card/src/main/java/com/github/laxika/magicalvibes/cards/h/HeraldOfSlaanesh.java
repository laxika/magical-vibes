package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "40K", collectorNumber = "77")
public class HeraldOfSlaanesh extends Card {

    public HeraldOfSlaanesh() {
        // Demon spells you cast cost {2} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DEMON), 2, CostModificationScope.SELF));
        // Other Demons you control have haste.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DEMON)));
    }
}
