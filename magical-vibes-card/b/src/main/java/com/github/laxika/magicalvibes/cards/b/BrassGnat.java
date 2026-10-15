package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "TSP", collectorNumber = "249")
public class BrassGnat extends Card {

    public BrassGnat() {
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{1}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {1} to untap Brass Gnat?"));
    }
}
