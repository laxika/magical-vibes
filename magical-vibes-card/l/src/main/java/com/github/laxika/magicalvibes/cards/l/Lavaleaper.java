package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "150")
@CardRegistration(set = "ECL", collectorNumber = "318")
public class Lavaleaper extends Card {

    public Lavaleaper() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_CREATURES_INCLUDING_SELF));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddOneOfEachManaTypeProducedByLandEffect(false,
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC)));
    }
}
