package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedWatchedCreaturesCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "35")
public class KangDynasty extends Card {

    public KangDynasty() {
        for (EffectSlot chapter : List.of(EffectSlot.SAGA_CHAPTER_I, EffectSlot.SAGA_CHAPTER_II)) {
            addEffect(chapter, new TapPermanentsEffect(TapUntapScope.TARGET));
            addEffect(chapter, new GoadTargetCreatureUntilNextTurnEffect());
            addEffect(chapter, new RegisterDelayedWatchedCreaturesCombatDamageEffect(
                    List.of(new DrawCardEffect()), true));
            setSagaChapterTargetGroups(chapter, List.of(
                    new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 99)));
        }
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new BoostTargetCreatureEffect(cardsInHand, cardsInHand));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new MakeCreatureUnblockableEffect());
        setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_III, List.of(
                new SagaChapterTargetGroup(TargetFilters.creatureYouControl(), 1, 1)));
    }
}
