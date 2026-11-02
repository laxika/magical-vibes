package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "49")
@CardRegistration(set = "LTC", collectorNumber = "132")
@CardRegistration(set = "LTC", collectorNumber = "452")
public class BoromirGondorsHope extends Card {

    public BoromirGondorsHope() {
        LookAtTopCardsEffect search = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.HUMAN),
                        new CardTypePredicate(CardType.ARTIFACT))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), search);
        addTrigger(Triggers.attacks(), search);
    }
}
