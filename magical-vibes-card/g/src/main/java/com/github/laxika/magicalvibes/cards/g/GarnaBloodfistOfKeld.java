package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "200")
public class GarnaBloodfistOfKeld extends Card {

    public GarnaBloodfistOfKeld() {
        // Whenever another creature you control dies, draw a card if it was attacking.
        addTrigger(Triggers.creatureDies(new PermanentIsAttackingPredicate(), ObjectRelation.ANOTHER_YOU_CONTROL),
                new DrawCardEffect(1));

        // Otherwise, Garna deals 1 damage to each opponent.
        addTrigger(Triggers.creatureDies(
                        new PermanentNotPredicate(new PermanentIsAttackingPredicate()),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
