package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "701")
public class OriginOfThor extends Card {

    public OriginOfThor() {
        addTrigger(Triggers.chapter(1), new MayEffect(
                new DiscardAndDrawCardEffect(1, 2),
                "Discard a card to draw two cards?"));

        var creatureYouControl = TargetFilters.creatureYouControl();
        addTrigger(Triggers.chapter(2),
                new RegisterDelayedControllerSpellCastTriggerEffect(
                        null,
                        List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        false,
                        creatureYouControl));

        addTrigger(Triggers.chapter(3), new TargetCreatureDealsPowerDamageToEachOpponentEffect());
        setSagaChapterTargetFilter(3, Set.of(creatureYouControl));
    }
}
