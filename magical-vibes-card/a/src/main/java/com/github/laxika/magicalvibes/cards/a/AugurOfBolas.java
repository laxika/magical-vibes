package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "43")
@CardRegistration(set = "WAR", collectorNumber = "41")
@CardRegistration(set = "MM3", collectorNumber = "30")
@CardRegistration(set = "C13", collectorNumber = "30")
public class AugurOfBolas extends Card {

    public AugurOfBolas() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(3,
                new CardAnyOfPredicate(List.of(new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)))));
    }
}
