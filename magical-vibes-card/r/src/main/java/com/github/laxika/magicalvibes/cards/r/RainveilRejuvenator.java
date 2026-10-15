package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "152")
public class RainveilRejuvenator extends Card {

    public RainveilRejuvenator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new MillEffect(3, MillRecipient.CONTROLLER), "Mill three cards?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, new SourcePower())),
                "{T}: Add an amount of {G} equal to Rainveil Rejuvenator's power."
        ));
    }
}
