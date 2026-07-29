package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "102")
public class WaveElemental extends Card {

    public WaveElemental() {
        // {U}, {T}, Sacrifice this creature: Tap up to three target creatures without flying.
        TargetFilter creatureWithoutFlying = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
                )),
                "Target must be a creature without flying"
        );

        addActivatedAbility(new ActivatedAbility(
                true, "{U}",
                List.of(new SacrificeSelfCost(), new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{U}, {T}, Sacrifice this creature: Tap up to three target creatures without flying.",
                List.of(creatureWithoutFlying, creatureWithoutFlying, creatureWithoutFlying),
                0,
                3
        ));
    }
}
