package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FEM", collectorNumber = "78")
public class TheloniteDruid extends Card {

    public TheloniteDruid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(
                        new SacrificeCreatureCost(),
                        new AnimatePermanentsEffect(
                                new Fixed(2), new Fixed(3),
                                List.of(), Set.of(),
                                null, Set.of(),
                                GrantScope.OWN_PERMANENTS, EffectDuration.UNTIL_END_OF_TURN,
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST))
                ),
                "{1}{G}, {T}, Sacrifice a creature: Forests you control become 2/3 creatures until end of turn. They're still lands."
        ));
    }
}
