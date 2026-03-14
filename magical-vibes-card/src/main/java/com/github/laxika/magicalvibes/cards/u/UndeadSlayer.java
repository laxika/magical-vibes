package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "37")
public class UndeadSlayer extends Card {

    public UndeadSlayer() {
        // {W}, {T}: Exile target Skeleton, Vampire, or Zombie.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new ExileTargetPermanentEffect()),
                "{W}, {T}: Exile target Skeleton, Vampire, or Zombie.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.SKELETON, CardSubtype.VAMPIRE, CardSubtype.ZOMBIE
                        )),
                        "Target must be a Skeleton, Vampire, or Zombie"
                )
        ));
    }
}
