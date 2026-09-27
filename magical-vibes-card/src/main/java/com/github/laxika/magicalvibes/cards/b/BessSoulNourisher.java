package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerToughnessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "67")
@CardRegistration(set = "NCC", collectorNumber = "167")
public class BessSoulNourisher extends Card {

    private static final PermanentPredicate OTHER_BASE_ONE_ONE = new PermanentAllOfPredicate(List.of(
            new PermanentBasePowerToughnessPredicate(1, 1),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

    public BessSoulNourisher() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(OTHER_BASE_ONE_ONE,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
        addEffect(EffectSlot.ON_ATTACK,
                new BoostAllOwnCreaturesEffect(
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                        OTHER_BASE_ONE_ONE));
    }
}
