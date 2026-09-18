package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerOrPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CMD", collectorNumber = "32")
public class SoulSnare extends Card {

    public SoulSnare() {
        var targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingSourceControllerOrPlaneswalkerPredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeSelfCost(), new ExileTargetPermanentEffect(targetPredicate)),
                "{W}, Sacrifice this enchantment: Exile target creature that's attacking you or a planeswalker you control.",
                new PermanentPredicateTargetFilter(
                        targetPredicate,
                        "Target must be a creature attacking you or a planeswalker you control")
        ));
    }
}
