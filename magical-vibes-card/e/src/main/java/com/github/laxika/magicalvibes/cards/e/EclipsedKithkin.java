package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "220")
@CardRegistration(set = "ECL", collectorNumber = "338")
public class EclipsedKithkin extends Card {

    public EclipsedKithkin() {
        CardAnyOfPredicate kithkinForestOrPlains = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.KITHKIN),
                new CardSubtypePredicate(CardSubtype.FOREST),
                new CardSubtypePredicate(CardSubtype.PLAINS)));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, kithkinForestOrPlains));
    }
}
