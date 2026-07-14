package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "65")
public class AerieOuphes extends Card {

    public AerieOuphes() {
        // Sacrifice this creature: It deals damage equal to its power to target creature with flying.
        // Persist is loaded from Scryfall and handled by PermanentRemovalService.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(new SourcePower())),
                "Sacrifice Aerie Ouphes: It deals damage equal to its power to target creature with flying.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        "Target must be a creature with flying"
                )
        ));
    }
}
