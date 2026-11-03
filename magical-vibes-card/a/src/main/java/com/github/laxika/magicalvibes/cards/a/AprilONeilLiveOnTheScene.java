package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "9")
@CardRegistration(set = "TMC", collectorNumber = "84")
public class AprilONeilLiveOnTheScene extends Card {

    public AprilONeilLiveOnTheScene() {
        var mutantNinjaOrTurtle = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.MUTANT),
                new PermanentHasSubtypePredicate(CardSubtype.NINJA),
                new PermanentHasSubtypePredicate(CardSubtype.TURTLE)));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, mutantNinjaOrTurtle)),
                        ObjectRelation.YOU_CONTROL),
                CreateTokenEffect.ofClueToken(1));
    }
}
