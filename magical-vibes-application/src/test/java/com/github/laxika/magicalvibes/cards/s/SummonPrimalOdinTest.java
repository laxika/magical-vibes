package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonPrimalOdin.class, Forest.class, GrizzlyBears.class})
class SummonPrimalOdinTest extends BaseCardTest {

    @Test
    void chapterIDestroysTargetCreatureAnOpponentControls() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId(), opponentLand.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, saga);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opponentCreature)
                .contains(opponentLand);
    }

    @Test
    void chapterIIGivesThisCreatureTheGameLossTrigger() {
        Permanent saga = addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        saga.setSummoningSick(false);
        saga.setAttacking(true);
        resolveCombat();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void chapterICannotSkipChoosingAnAvailableTarget() {
        addSagaWithLore(0);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreature.getId())
                .doesNotContain(player1.getId(), player2.getId());
    }

    @Test
    void enteringBattlefieldTriggersChapterI() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new SummonPrimalOdin());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature.getCard());
    }

    @Test
    void combatDamageBeforeChapterIIDoesNotCauseGameLoss() {
        Permanent saga = addSagaWithLore(1);
        saga.setSummoningSick(false);
        saga.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertLife(player2, 15);
    }

    @Test
    void repeatingChapterIIGrantsAnAdditionalIndependentCombatDamageTrigger() {
        Permanent saga = addSagaWithLore(1);
        advanceToNextChapter();
        resolveAllTriggers();

        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();
        resolveAllTriggers();

        saga.setSummoningSick(false);
        saga.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void chapterIIIDrawsTwoCardsAndEachPlayerLosesTwoLife() {
        Permanent saga = addSagaWithLore(2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonPrimalOdin());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
