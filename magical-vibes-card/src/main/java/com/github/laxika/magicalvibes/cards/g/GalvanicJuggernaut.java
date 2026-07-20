package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "INR", collectorNumber = "263")
@CardRegistration(set = "ISD", collectorNumber = "222")
public class GalvanicJuggernaut extends Card {

    public GalvanicJuggernaut() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // This creature doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // Whenever another creature dies, untap this creature.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new UntapPermanentsEffect(TapUntapScope.SELF));
    }
}
