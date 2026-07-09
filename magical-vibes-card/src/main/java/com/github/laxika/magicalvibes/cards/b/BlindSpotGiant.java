package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "153")
public class BlindSpotGiant extends Card {

    public BlindSpotGiant() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.GIANT)),
                "you control another Giant"
        ));
    }
}
