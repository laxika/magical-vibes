package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "202")
public class SwathcutterGiant extends Card {

    public SwathcutterGiant() {
        // Whenever this creature attacks, it deals 1 damage to each creature defending player controls.
        addTrigger(Triggers.attacks(), DealDamageEffect.toEach(
                Subjects.controlledBy(Subjects.defendingPlayer(), new PermanentIsCreaturePredicate()),
                1));
    }
}
