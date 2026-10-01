package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RippleEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "47")
public class SurgingAether extends Card {

    public SurgingAether() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new MayEffect(new RippleEffect(4), "Reveal the top four cards of your library?"));
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
