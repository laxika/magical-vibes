package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.NihiloorAttackEffect;
import com.github.laxika.magicalvibes.model.effect.NihiloorEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "53")
public class Nihiloor extends Card {

    public Nihiloor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new NihiloorEnterEffect());
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate())),
                new NihiloorAttackEffect());
    }
}
