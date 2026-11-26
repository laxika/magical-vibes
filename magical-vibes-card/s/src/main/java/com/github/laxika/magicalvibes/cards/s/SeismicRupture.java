package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "FDN", collectorNumber = "205")
@CardRegistration(set = "DTK", collectorNumber = "156")
public class SeismicRupture extends Card {

    public SeismicRupture() {
        addSpellEffect(DealDamageEffect.toEachCreature(2,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
