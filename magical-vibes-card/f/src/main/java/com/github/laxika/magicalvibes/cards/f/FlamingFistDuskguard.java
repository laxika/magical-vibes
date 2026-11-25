package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "22")
public class FlamingFistDuskguard extends Card {

    public FlamingFistDuskguard() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                RegisterDelayedControllerSpellCastTriggerEffect.boon(
                        new CardTypePredicate(CardType.CREATURE),
                        List.of(PerpetuallyBoostCardEffect.toCards(Subjects.cardsOf(Subjects.triggeringSpell()), 1, 0))));
    }
}
