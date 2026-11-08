package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "125")
public class GreatbowDoyen extends Card {

    public GreatbowDoyen() {
        // Other Archer creatures you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ARCHER)));

        // Whenever an Archer you control deals damage to a creature, that Archer deals that much
        // damage to that creature's controller.
        addTrigger(Triggers.creatureYouControlDealsDamageToCreature().withFilter(new PermanentHasSubtypePredicate(CardSubtype.ARCHER)),
                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)
                        .dealtByEventDamageSource());
    }
}
