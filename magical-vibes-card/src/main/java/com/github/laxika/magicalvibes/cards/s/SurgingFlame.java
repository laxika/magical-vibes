package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RippleEffect;

@CardRegistration(set = "CSP", collectorNumber = "99")
public class SurgingFlame extends Card {

    public SurgingFlame() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new MayEffect(new RippleEffect(4), "Reveal the top four cards of your library?"));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
