package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "78")
@CardRegistration(set = "DBL", collectorNumber = "345")
public class SkywarpSkaab extends Card {

    public SkywarpSkaab() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new ExileNCardsFromGraveyardThenEffect(2, new CardTypePredicate(CardType.CREATURE),
                        new DrawCardEffect(1)),
                "Exile two creature cards from your graveyard?"));
    }
}
