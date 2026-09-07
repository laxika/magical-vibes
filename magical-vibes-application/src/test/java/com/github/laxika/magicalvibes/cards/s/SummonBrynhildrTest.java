package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonBrynhildr.class, Shock.class, GrizzlyBears.class})
class SummonBrynhildrTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles the top card and grants play permission until end of turn")
    void chapterIExilesTopCardAndGrantsPlayPermission() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        addSaga(0);

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Chapter II grants haste to only the next creature spell")
    void chapterIIGrantsHasteToNextCreatureSpellOnly() {
        addSaga(1);

        advanceToNextChapter();
        resolveAllTriggers();

        Permanent firstCreature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isTrue();

        Permanent secondCreature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Chapter III's haste trigger works after the Saga is sacrificed")
    void chapterIIITriggerSurvivesSagaLeavingTheBattlefield() {
        addSaga(2);

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SummonBrynhildr);

        Permanent creature = castCreatureAndResolve();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonBrynhildr());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent castCreatureAndResolve() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
