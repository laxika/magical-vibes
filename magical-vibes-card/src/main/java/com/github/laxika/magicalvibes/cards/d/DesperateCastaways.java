package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessControllerControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "XLN", collectorNumber = "101")
public class DesperateCastaways extends Card {

    public DesperateCastaways() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessControllerControlsMatchingPermanentEffect(
                new PermanentIsArtifactPredicate(),
                "you control an artifact"
        ));
    }
}
