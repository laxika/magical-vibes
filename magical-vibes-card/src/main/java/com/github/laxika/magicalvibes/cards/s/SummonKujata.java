package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "61")
@CardRegistration(set = "FIC", collectorNumber = "199")
public class SummonKujata extends Card {

    public SummonKujata() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToTargetCreatureEffect(3));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2)));

        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 3)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new DrawCardEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new DealDamageToPlayersEffect(
                new LastDiscardedCardManaValue(), DamageRecipient.EACH_OPPONENT));
    }
}
