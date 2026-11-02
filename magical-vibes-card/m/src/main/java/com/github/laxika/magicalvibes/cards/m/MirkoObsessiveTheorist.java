package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "2")
@CardRegistration(set = "MKC", collectorNumber = "50")
@CardRegistration(set = "MKC", collectorNumber = "316")
public class MirkoObsessiveTheorist extends Card {

    public MirkoObsessiveTheorist() {
        addEffect(EffectSlot.ON_CONTROLLER_SURVEILS,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerLessThanSourcePowerPredicate())))
                        .targetGraveyard(true)
                        .enterWithCounter(CounterType.FINALITY)
                        .enterWithCounterCount(1)
                        .build(),
                "Return the target creature card to the battlefield with a finality counter on it?"));
    }
}
