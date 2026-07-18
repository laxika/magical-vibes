package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCanAttackAsThoughHasteEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "304")
@CardRegistration(set = "4ED", collectorNumber = "252")
public class InstillEnergy extends Card {

    public InstillEnergy() {
        // Enchant creature — the enchanted creature can attack as though it had haste.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC, new EnchantedCreatureCanAttackAsThoughHasteEffect());

        // {0}: Untap enchanted creature. Activate only during your turn and only once each turn.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new UntapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{0}: Untap enchanted creature. Activate only during your turn and only once each turn.",
                null, null, 1, ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));
    }
}
