package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "PTK", collectorNumber = "122")
public class RollingEarthquake extends Card {

    public RollingEarthquake() {
        // Deals X damage to each creature without horsemanship and each player.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                0, true, true,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.HORSEMANSHIP))));
    }
}
