package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "86")
public class DwarvenScorcher extends Card {

    public DwarvenScorcher() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new DealDamageToTargetCreatureUnlessControllerTakesDamageEffect(1, 2)
                ),
                "Sacrifice this creature: This creature deals 1 damage to target creature unless that creature's controller has this creature deal 2 damage to them.",
                TargetFilters.creature()
        ));
    }
}
