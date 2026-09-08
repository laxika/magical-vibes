package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "251")
public class SpikedPitTrap extends Card {

    public SpikedPitTrap() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new SacrificeSelfCost(),
                        new RollD20Effect(
                                new DealDamageToTargetCreatureEffect(5),
                                SequenceEffect.of(
                                        new DealDamageToTargetCreatureEffect(5),
                                        CreateTokenEffect.ofTreasureToken(1)))
                ),
                "{5}, {T}, Sacrifice Spiked Pit Trap: Choose target creature, then roll a d20. 1-9 | This artifact deals 5 damage to that creature. 10-20 | This artifact deals 5 damage to that creature. Create a Treasure token.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"))
        );
    }
}
