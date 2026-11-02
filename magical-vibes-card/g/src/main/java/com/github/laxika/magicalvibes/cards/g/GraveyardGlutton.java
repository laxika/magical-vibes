package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class GraveyardGlutton extends Card {

    public GraveyardGlutton() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), exileFromGraveyard());
        addTrigger(Triggers.attacks(), exileFromGraveyard());
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessDiscardsEffect());
    }

    private ExileCardsFromGraveyardEffect exileFromGraveyard() {
        return new ExileCardsFromGraveyardEffect(
                2,
                new CardTypePredicate(CardType.CREATURE),
                1,
                1,
                false,
                true
        );
    }
}
