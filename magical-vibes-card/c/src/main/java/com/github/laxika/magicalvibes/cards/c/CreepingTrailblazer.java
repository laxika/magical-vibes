package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "207")
public class CreepingTrailblazer extends Card {

    public CreepingTrailblazer() {
        // Other Elementals you control get +1/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ELEMENTAL))));

        // {2}{R}{G}: This creature gets +1/+1 until end of turn for each Elemental you control.
        PermanentCount elementalsYouControl = new PermanentCount(
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ELEMENTAL)), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{G}",
                List.of(new BoostSelfEffect(elementalsYouControl, elementalsYouControl)),
                "{2}{R}{G}: This creature gets +1/+1 until end of turn for each Elemental you control."
        ));
    }
}
