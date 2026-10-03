package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

import java.util.Set;

@CardRegistration(set = "VOC", collectorNumber = "144")
public class BloodswornSteward extends Card {

    public BloodswornSteward() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                2, 2, Set.of(Keyword.HASTE), GrantScope.ALL_OWN_CREATURES,
                new PermanentIsCommanderPredicate()));
    }
}
