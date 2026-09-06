package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "TMT", collectorNumber = "159")
public class MouserMarkIII extends Card {

    public MouserMarkIII() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new ControlsAnotherPermanent(new PermanentIsArtifactPredicate()),
                "you control another artifact"
        ));
    }
}
