package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "208")
public class MalcatorPurityOverseer extends Card {

    public MalcatorPurityOverseer() {
        CreateTokenEffect golem = new CreateTokenEffect(
                "Phyrexian Golem", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(),
                Set.of(CardType.ARTIFACT));

        addTrigger(Triggers.enters(ObjectRelation.THIS), golem);
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new PermanentEnteredThisTurn(new CardTypePredicate(CardType.ARTIFACT), 3)),
                golem);
    }
}
