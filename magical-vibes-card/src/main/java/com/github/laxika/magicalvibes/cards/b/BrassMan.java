package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "4ED", collectorNumber = "302")
public class BrassMan extends Card {

    public BrassMan() {
        // This creature doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // At the beginning of your upkeep, you may pay {1}. If you do, untap this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{1}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {1} to untap Brass Man?"));
    }
}
