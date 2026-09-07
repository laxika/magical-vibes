package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ELD", collectorNumber = "114")
public class BlowYourHouseDown extends Card {

    public BlowYourHouseDown() {
        target(TargetFilters.creature(), 0, 3)
                .addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL,
                        new DestroyEachTargetPermanentEffect(new PermanentHasSubtypePredicate(CardSubtype.WALL)));
    }
}
