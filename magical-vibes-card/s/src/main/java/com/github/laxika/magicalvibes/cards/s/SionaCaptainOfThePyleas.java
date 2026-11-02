package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "226")
@CardRegistration(set = "WOC", collectorNumber = "144")
public class SionaCaptainOfThePyleas extends Card {

    public SionaCaptainOfThePyleas() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(7, new CardIsAuraPredicate()));
        addTrigger(Triggers.auraYouControlBecomesAttachedToCreatureYouControl(),
                new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
