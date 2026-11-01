package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "9")
@CardRegistration(set = "TMC", collectorNumber = "84")
public class AprilONeilLiveOnTheScene extends Card {

    public AprilONeilLiveOnTheScene() {
        CardPredicate mutantNinjaOrTurtle = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.MUTANT),
                new CardSubtypePredicate(CardSubtype.NINJA),
                new CardSubtypePredicate(CardSubtype.TURTLE)));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        mutantNinjaOrTurtle,
                        CreateTokenEffect.ofClueToken(1)));
    }
}
