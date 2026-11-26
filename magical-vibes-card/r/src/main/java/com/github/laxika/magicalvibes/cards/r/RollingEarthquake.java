package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "PTK", collectorNumber = "122")
@CardRegistration(set = "V14", collectorNumber = "10")
@CardRegistration(set = "ME3", collectorNumber = "110")
@CardRegistration(set = "2XM", collectorNumber = "142")
public class RollingEarthquake extends Card {

    public RollingEarthquake() {
        // Deals X damage to each creature without horsemanship and each player.
        addSpellEffect(DealDamageEffect.toEachCreature(new XValue(),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.HORSEMANSHIP)))
                .andTo(Subjects.eachPlayer()));
    }
}
