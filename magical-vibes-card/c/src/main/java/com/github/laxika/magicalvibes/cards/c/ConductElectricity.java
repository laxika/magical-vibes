package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "130")
public class ConductElectricity extends Card {

    public ConductElectricity() {
        setAllowSharedTargets(true);

        DealDamageToTargetCreatureEffect creatureDamage = new DealDamageToTargetCreatureEffect(6);
        DealDamageToTargetCreatureEffect tokenDamage = new DealDamageToTargetCreatureEffect(2);
        target(TargetFilters.creature()).bindEffect(creatureDamage);

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTokenPredicate()
                )),
                "Target must be a creature token"
        ), 0, 1).bindEffect(tokenDamage);
        addSimultaneousSpellEffects(creatureDamage, tokenDamage);
    }
}
