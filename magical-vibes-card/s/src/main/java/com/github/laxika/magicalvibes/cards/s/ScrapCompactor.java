package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "242")
public class ScrapCompactor extends Card {

    public ScrapCompactor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(3)),
                "{3}, {T}, Sacrifice this artifact: It deals 3 damage to target creature.",
                TargetFilters.creature()
        ));

        PermanentPredicate creatureOrVehicle = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{6}, {T}, Sacrifice this artifact: Destroy target creature or Vehicle.",
                new PermanentPredicateTargetFilter(creatureOrVehicle,
                        "Target must be a creature or Vehicle")
        ));
    }
}
