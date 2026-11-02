package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "11")
public class EagerGlyphmage extends Card {

    public EagerGlyphmage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                1,
                "Inkling",
                1,
                1,
                CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.INKLING),
                Set.of(Keyword.FLYING),
                Set.of()
        ));
    }
}
