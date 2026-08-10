package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "MRD", collectorNumber = "177")
public class GoblinDirigible extends Card {

    public GoblinDirigible() {
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{4}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {4} to untap Goblin Dirigible?"));
    }
}
