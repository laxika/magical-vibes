package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DMU", collectorNumber = "200")
public class GarnaBloodfistOfKeld extends Card {

    public GarnaBloodfistOfKeld() {
        // Whenever another creature you control dies, draw a card if it was attacking.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(), new DrawCardEffect(1)));

        // Otherwise, Garna deals 1 damage to each opponent.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentIsAttackingPredicate()),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)));
    }
}
