package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "38")
public class VeteranArmorsmith extends Card {

    public VeteranArmorsmith() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, Set.of(), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER))));
    }
}
