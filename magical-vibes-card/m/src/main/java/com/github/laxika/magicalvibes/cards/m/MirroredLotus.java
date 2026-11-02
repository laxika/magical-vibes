package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReflectEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "107")
public class MirroredLotus extends Card {

    public MirroredLotus() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReflectEffect("{0}"));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileSelfCost(), new AwardAnyColorManaEffect(3)),
                "{T}, Exile Mirrored Lotus: Add three mana of any one color."
        ));
    }
}
