package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.TapEnchantedPermanentCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "54")
public class KrovikanPlague extends Card {

    public KrovikanPlague() {
        // Enchant non-Wall creature you control
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL))
                )),
                "Target must be a non-Wall creature you control"
        ));

        // When this Aura enters, draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterDrawCardsAtNextUpkeepEffect());

        // Tap enchanted creature: This Aura deals 1 damage to any target. Put a -0/-1 counter on
        // enchanted creature. Activate only if enchanted creature is untapped.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new TapEnchantedPermanentCost(),
                        new DealDamageToAnyTargetEffect(1),
                        new PutCounterOnReferencedPermanentEffect(CounterType.MINUS_ZERO_MINUS_ONE)),
                "Tap enchanted creature: Krovikan Plague deals 1 damage to any target. "
                        + "Put a -0/-1 counter on enchanted creature. "
                        + "Activate only if enchanted creature is untapped."));
    }
}
