package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M14", collectorNumber = "158")
public class ThorncasterSliver extends Card {

    public ThorncasterSliver() {
        // Thorncaster Sliver is itself a Sliver, so ALL_OWN_CREATURES (source must pass the filter too).
        // The granted attack trigger is collected off each attacking Sliver in CombatAttackService and
        // routed through the AttackTriggerTarget interaction, with that Sliver as the damage source.
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(EffectSlot.ON_ATTACK,
                new DealDamageToAnyTargetEffect(1), GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
