package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "TMT", collectorNumber = "159")
public class MouserMarkIII extends Card {

    public MouserMarkIII() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new ControlsAnotherPermanent(new PermanentIsArtifactPredicate()),
                "you control another artifact"
        ));
    }
}
