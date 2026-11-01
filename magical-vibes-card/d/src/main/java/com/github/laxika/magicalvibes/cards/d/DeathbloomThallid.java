package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "84")
@CardRegistration(set = "M21", collectorNumber = "94")
public class DeathbloomThallid extends Card {

    public DeathbloomThallid() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect(
                "Saproling",
                1,
                1,
                CardColor.GREEN,
                List.of(CardSubtype.SAPROLING),
                Set.of(),
                Set.of()
        ));
    }
}
