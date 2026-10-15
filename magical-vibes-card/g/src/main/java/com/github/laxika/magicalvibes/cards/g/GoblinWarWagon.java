package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "MRD", collectorNumber = "179")
public class GoblinWarWagon extends Card {

    public GoblinWarWagon() {
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{2}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {2} to untap Goblin War Wagon?"));
    }
}
