package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "147")
@CardRegistration(set = "TDC", collectorNumber = "221")
@CardRegistration(set = "FDC", collectorNumber = "162")
public class LeylineTyrant extends Card {

    public LeylineTyrant() {
        addStaticEffect(new PreventManaDrainEffect(ManaColor.RED));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect(ManaColor.RED));
    }
}
