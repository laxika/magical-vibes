package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "96")
@CardRegistration(set = "M15", collectorNumber = "139")
@CardRegistration(set = "IMA", collectorNumber = "122")
@CardRegistration(set = "GN3", collectorNumber = "70")
@CardRegistration(set = "C17", collectorNumber = "133")
@CardRegistration(set = "SCD", collectorNumber = "129")
@CardRegistration(set = "FDC", collectorNumber = "148")
public class CrucibleOfFire extends Card {

    public CrucibleOfFire() {
        // Dragon creatures you control get +3/+3.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 3, Set.of(), GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.DRAGON))));
    }
}
