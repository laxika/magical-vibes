package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "197")
public class LifesparkSpellbomb extends Card {

    public LifesparkSpellbomb() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new AnimatePermanentsEffect(
                                3, 3, List.of(), Set.of(), null, Set.of(),
                                GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN
                        )
                ),
                "{G}, Sacrifice this artifact: Until end of turn, target land becomes a 3/3 creature that's still a land.",
                TargetFilters.land()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, Sacrifice this artifact: Draw a card."
        ));
    }
}
