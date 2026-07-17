package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "244")
public class IronclawCurse extends Card {

    public IronclawCurse() {
        // Enchant creature; enchanted creature gets -0/-1 and can't block creatures with power >=
        // the enchanted creature's (effective) toughness. The can't-block restriction is read at
        // declare-blockers time by GameQueryService.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, -1, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect());
    }
}
