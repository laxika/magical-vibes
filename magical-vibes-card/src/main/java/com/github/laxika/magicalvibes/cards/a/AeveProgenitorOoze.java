package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MH2", collectorNumber = "148")
public class AeveProgenitorOoze extends Card {

    public AeveProgenitorOoze() {
        PermanentCount otherOozes = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.OOZE), CountScope.CONTROLLER, true);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, otherOozes));
        addEffect(EffectSlot.ON_SELF_CAST, StormEffect.tokenCopyWithoutLegendary());
    }
}
