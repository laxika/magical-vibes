package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "187")
public class LegionGuildmage extends Card {

    public LegionGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{R}",
                List.of(DealDamageEffect.to(Subjects.eachOpponent(), 3)),
                "{5}{R}, {T}: This creature deals 3 damage to each opponent."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(new TapPermanentsEffect(Subjects.target())),
                "{2}{W}, {T}: Tap another target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature"
                )
        ));
    }
}
