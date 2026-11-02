package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "14")
public class GuardedHeir extends Card {

    public GuardedHeir() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                2, "Knight", 3, 3, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(), Set.of()
        ));
    }
}
