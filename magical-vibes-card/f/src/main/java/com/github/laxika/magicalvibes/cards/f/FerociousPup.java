package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "171")
public class FerociousPup extends Card {

    public FerociousPup() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                "Wolf",
                2,
                2,
                CardColor.GREEN,
                List.of(CardSubtype.WOLF),
                Set.of(),
                Set.of()
        ));
    }
}
