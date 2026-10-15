package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "46")
@CardRegistration(set = "ECL", collectorNumber = "356")
public class ChampionsOfTheShoal extends Card {

    public ChampionsOfTheShoal() {
        addEffect(EffectSlot.SPELL, new BeholdAndExileCost(CardSubtype.MERFOLK));

        var tapAndStun = SequenceEffect.of(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN))
                .addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentIsSourceCardPredicate(),
                                tapAndStun));
    }
}
