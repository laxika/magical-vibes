package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "90")
public class FireJuggler extends Card {

    public FireJuggler() {
        // Whenever this creature becomes blocked, clash with an opponent. If you win,
        // this creature deals 4 damage to each creature blocking it.
        addTrigger(Triggers.becomesBlocked(), new ClashEffect(
                new DealDamageToEachMatchingPermanentEffect(
                        4, new PermanentInCombatWithSourcePredicate(), EachPermanentScope.ALL_PLAYERS)));
    }
}
