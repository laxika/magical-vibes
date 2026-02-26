package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.UntapEachOtherCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "181")
public class MyrGalvanizer extends Card {

    public MyrGalvanizer() {
        // Other Myr creatures you control get +1/+1
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MYR))));

        // {1}, {T}: Untap each other Myr you control.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new UntapEachOtherCreatureYouControlEffect(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.MYR)))),
                "{1}, {T}: Untap each other Myr you control."
        ));
    }
}
