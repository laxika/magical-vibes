package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "RIX", collectorNumber = "113")
public class ShakeTheFoundations extends Card {

    public ShakeTheFoundations() {
        addSpellEffect(DealDamageEffect.toEachCreature(1,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addSpellEffect(new DrawCardEffect(1));
    }
}
