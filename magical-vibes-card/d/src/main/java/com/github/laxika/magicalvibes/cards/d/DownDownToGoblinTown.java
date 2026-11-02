package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "65")
public class DownDownToGoblinTown extends Card {

    public DownDownToGoblinTown() {
        addTrigger(Triggers.chapter(1), new ChooseCardsFromTargetHandEffect(
                1, List.of(CardType.LAND), HandChoiceDestination.DISCARD));
        setSagaChapterTargetFilter(1, Set.of(opponentTargetFilter()));

        addTrigger(Triggers.chapter(2), new AmassGoblinsEffect(1));

        addLifeDrainChapter(3);
        addLifeDrainChapter(4);
    }

    private void addLifeDrainChapter(int chapter) {
        addTrigger(Triggers.chapter(chapter), new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addTrigger(Triggers.chapter(chapter), new GainLifeEffect(1));
        setSagaChapterTargetFilter(chapter, Set.of(opponentTargetFilter()));
    }

    private static PlayerPredicateTargetFilter opponentTargetFilter() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent");
    }
}
