package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ARN", collectorNumber = "2")
public class ArmyOfAllah extends Card {

    public ArmyOfAllah() {
        // Attacking creatures get +2/+0 until end of turn.
        addSpellEffect(BoostEffect.allCreatures(2, 0, new PermanentIsAttackingPredicate()));
    }
}
