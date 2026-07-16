package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DRB", collectorNumber = "14")
public class ThunderDragon extends Card {

    public ThunderDragon() {
        // When this creature enters, it deals 3 damage to each creature without flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MassDamageEffect(3, false, false,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
