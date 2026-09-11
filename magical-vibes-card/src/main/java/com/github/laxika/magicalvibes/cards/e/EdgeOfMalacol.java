package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersInsteadOfUntappingEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "OPC2", collectorNumber = "13")
public class EdgeOfMalacol extends Card {

    public EdgeOfMalacol() {
        PermanentIsCreaturePredicate creatures = new PermanentIsCreaturePredicate();
        addEffect(EffectSlot.STATIC, new PutCountersInsteadOfUntappingEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 2, creatures));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, creatures));
    }
}
