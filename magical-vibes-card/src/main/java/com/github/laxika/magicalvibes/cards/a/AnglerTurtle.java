package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "GN3", collectorNumber = "23")
public class AnglerTurtle extends Card {

    public AnglerTurtle() {
        // Creatures your opponents control attack each combat if able.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesMustAttackEffect(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
