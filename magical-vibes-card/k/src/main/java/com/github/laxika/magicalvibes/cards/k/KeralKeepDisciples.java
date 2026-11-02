package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringLoyaltyAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "334")
public class KeralKeepDisciples extends Card {

    public KeralKeepDisciples() {
        // Whenever you activate a loyalty ability of a Chandra planeswalker, this creature deals 1
        // damage to each opponent.
        addTrigger(Triggers.youActivateAbility(),
                new TriggeringLoyaltyAbilityConditionalEffect(
                        new TriggeringPermanentConditionalEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.CHANDRA),
                                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))));
    }
}
