package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "61")
@CardRegistration(set = "FIC", collectorNumber = "199")
public class SummonKujata extends Card {

    public SummonKujata() {
        addTrigger(Triggers.chapter(1), new DealDamageToTargetCreatureEffect(3));
        setSagaChapterTargetGroups(1, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2)));

        addTrigger(Triggers.chapter(2),
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
        setSagaChapterTargetGroups(2, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 3)));

        addTrigger(Triggers.chapter(3), new DiscardCardThenEffect(
                null,
                new DealDamageToPlayersEffect(new LastDiscardedCardManaValue(), DamageRecipient.EACH_OPPONENT),
                "a card"));
        addTrigger(Triggers.chapter(3), new DrawCardEffect(2));
    }
}
