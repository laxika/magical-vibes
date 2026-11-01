package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "104")
@CardRegistration(set = "PC2", collectorNumber = "46")
@CardRegistration(set = "UMA", collectorNumber = "134")
@CardRegistration(set = "PCA", collectorNumber = "46")
@CardRegistration(set = "2X2", collectorNumber = "113")
public class HissingIguanar extends Card {

    public HissingIguanar() {
        // Whenever another creature dies, you may have this creature deal 1 damage to
        // target player or planeswalker.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new MayEffect(
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                "Deal 1 damage to target player or planeswalker?"));
    }
}
