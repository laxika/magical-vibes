package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "82")
@CardRegistration(set = "DMR", collectorNumber = "172")
public class PenumbraBobcat extends Card {

    public PenumbraBobcat() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                "Cat",
                2,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.CAT),
                Set.of(),
                Set.of()
        ));
    }
}
