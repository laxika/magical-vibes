package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "54")
public class DisplacerBeast extends Card {

    public DisplacerBeast() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new VentureIntoDungeonEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(ReturnToHandEffect.self()),
                "{3}{U}: Return this creature to its owner's hand."
        ));
    }
}
