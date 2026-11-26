package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "XLN", collectorNumber = "101")
public class DesperateCastaways extends Card {

    public DesperateCastaways() {
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new ControlsPermanentCount(1, new PermanentIsArtifactPredicate()),
                "you control an artifact"
        ));
    }
}
