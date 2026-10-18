package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "TLA", collectorNumber = "239")
public class SandbenderScavengers extends Card {

    public SandbenderScavengers() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new ExileSourceCardFromGraveyardThenEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .dynamicMaxManaValue(new SourcePower(true))
                                .targetGraveyard(true)
                                .build()),
                "Exile Sandbender Scavengers?"));
    }
}
