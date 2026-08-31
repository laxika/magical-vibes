package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "68")
public class SnaremasterSprite extends Card {

    public SnaremasterSprite() {
        setCastTimeTargetFilter(TargetFilters.creatureAnOpponentControls());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                MayPayManaEffect.reflexiveTarget("{2}",
                        SequenceEffect.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new PutCounterOnTargetPermanentEffect(CounterType.STUN)),
                        "Pay {2} to tap target creature an opponent controls and put a stun counter on it?"));
    }
}
