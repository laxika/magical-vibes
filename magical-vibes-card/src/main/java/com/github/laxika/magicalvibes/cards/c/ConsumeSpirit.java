package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "131")
@CardRegistration(set = "9ED", collectorNumber = "119")
@CardRegistration(set = "M10", collectorNumber = "89")
public class ConsumeSpirit extends Card {

    public ConsumeSpirit() {
        setXColorRestriction(ManaColor.BLACK);

        // Deals X damage to any target...
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));

        // ...and you gain X life (equal to X, not to the damage dealt — see Consume Spirit's
        // ruling; unlike Corrupt, prevention does not reduce the life gained).
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
