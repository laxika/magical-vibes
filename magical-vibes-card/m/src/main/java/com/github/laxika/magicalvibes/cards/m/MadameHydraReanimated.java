package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "538")
public class MadameHydraReanimated extends Card {

    public MadameHydraReanimated() {
        CardsInGraveyard creatureCards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addStaticEffect(BoostEffect.self(creatureCards, creatureCards));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, Subjects.you()));
        addTrigger(Triggers.attacks(), new MillEffect(2, Subjects.you()));
    }
}
