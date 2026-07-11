package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "PTK", collectorNumber = "133")
public class BorrowingTheEastWind extends Card {

    public BorrowingTheEastWind() {
        // Deals X damage to each creature with horsemanship and each player.
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                0, true, true,
                new PermanentHasKeywordPredicate(Keyword.HORSEMANSHIP)));
    }
}
