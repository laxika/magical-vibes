package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "59")
public class MasterOfThePearlTrident extends Card {

    public MasterOfThePearlTrident() {
        // Other Merfolk creatures you control get +1/+1 and have islandwalk.
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.ISLANDWALK), GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)));
    }
}
