package com.github.laxika.magicalvibes.cards.u;

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

@CardRegistration(set = "GTC", collectorNumber = "27")
@CardRegistration(set = "A25", collectorNumber = "37")
@CardRegistration(set = "MM3", collectorNumber = "27")
@CardRegistration(set = "RVR", collectorNumber = "33")
public class UrbisProtector extends Card {

    public UrbisProtector() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                "Angel",
                4,
                4,
                CardColor.WHITE,
                List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING),
                Set.of()
        ));
    }
}
