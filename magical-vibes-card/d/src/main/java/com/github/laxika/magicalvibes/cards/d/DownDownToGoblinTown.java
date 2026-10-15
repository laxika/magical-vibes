package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "65")
public class DownDownToGoblinTown extends Card {

    public DownDownToGoblinTown() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ChooseCardsFromTargetHandEffect(
                1, List.of(CardType.LAND), HandChoiceDestination.DISCARD));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(opponentTargetFilter()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new AmassGoblinsEffect(1));

        addLifeDrainChapter(EffectSlot.SAGA_CHAPTER_III);
        addLifeDrainChapter(EffectSlot.SAGA_CHAPTER_IV);
    }

    private void addLifeDrainChapter(EffectSlot slot) {
        addEffect(slot, new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(slot, new GainLifeEffect(1));
        setSagaChapterTargetFilter(slot, Set.of(opponentTargetFilter()));
    }

    private static PlayerPredicateTargetFilter opponentTargetFilter() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent");
    }
}
