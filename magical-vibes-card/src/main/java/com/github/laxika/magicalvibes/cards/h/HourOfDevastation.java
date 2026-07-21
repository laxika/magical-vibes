package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "HOU", collectorNumber = "97")
public class HourOfDevastation extends Card {

    public HourOfDevastation() {
        // All creatures lose indestructible until end of turn.
        addEffect(EffectSlot.SPELL,
                new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_CREATURES));

        // Deals 5 damage to each creature and each non-Bolas planeswalker.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(5, false, false, true,
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.BOLAS))));
    }
}
