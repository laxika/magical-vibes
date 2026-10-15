package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "19")
public class KithkinArmor extends Card {

    public KithkinArmor() {
        target(TargetFilters.creature())
                // Enchanted creature can't be blocked by creatures with power 3 or greater.
                .addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentPowerAtLeastPredicate(3)));
        // Sacrifice this Aura: The next time a source of your choice would deal damage to
        // enchanted creature this turn, prevent that damage.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), PreventDamageFromChosenSourceEffect.nextDamageToEnchantedCreature()),
                "Sacrifice this Aura: The next time a source of your choice would deal damage to enchanted creature this turn, prevent that damage."));
    }
}
