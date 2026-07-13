package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "14")
public class DarajaGriffin extends Card {

    public DarajaGriffin() {
        // Flying is auto-loaded from Scryfall keywords.
        // Sacrifice Daraja Griffin: Destroy target black creature.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect(false)),
                "Sacrifice Daraja Griffin: Destroy target black creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK))
                        )),
                        "Target must be a black creature"
                )
        ));
    }
}
