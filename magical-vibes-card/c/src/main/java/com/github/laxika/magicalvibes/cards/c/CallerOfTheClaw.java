package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.NontokenCreaturesPutIntoOwnGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "121")
@CardRegistration(set = "C15", collectorNumber = "177")
public class CallerOfTheClaw extends Card {

    public CallerOfTheClaw() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                new NontokenCreaturesPutIntoOwnGraveyardThisTurn(),
                "Bear", 2, 2, CardColor.GREEN, List.of(CardSubtype.BEAR), Set.of(), Set.of()));
    }
}
