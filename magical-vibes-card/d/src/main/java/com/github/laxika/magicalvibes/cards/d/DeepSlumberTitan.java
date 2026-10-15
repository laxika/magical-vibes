package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "SHM", collectorNumber = "89")
public class DeepSlumberTitan extends Card {

    public DeepSlumberTitan() {
        // This creature enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // This creature doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // Whenever this creature is dealt damage, untap it.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
