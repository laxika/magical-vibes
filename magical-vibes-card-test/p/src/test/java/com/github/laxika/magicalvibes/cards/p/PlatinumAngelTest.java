package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MortalCombat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlatinumAngel.class, Shock.class, MortalCombat.class, GrizzlyBears.class})
class PlatinumAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Controller doesn't lose at 0 life with Platinum Angel")
    void controllerDoesNotLoseAtZeroLife() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, 2);

        // Shock player1 to bring them to 0
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Controller doesn't lose at negative life with Platinum Angel")
    void controllerDoesNotLoseAtNegativeLife() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Player loses normally without Platinum Angel at 0 life")
    void playerLosesNormallyWithoutPlatinumAngel() {
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Player loses normally after Platinum Angel is removed")
    void playerLosesAfterPlatinumAngelRemoved() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, 0);

        // Remove Platinum Angel
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent's Platinum Angel does not protect you from losing")
    void opponentsPlatinumAngelDoesNotProtectYou() {
        harness.addToBattlefield(player2, new PlatinumAngel());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void controllerDoesNotLoseWithTenPoisonCounters() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void controllerDoesNotLoseWhenDrawingFromEmptyLibrary() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.runStateBasedActions();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void opponentCannotWinWithMortalCombat() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.addToBattlefield(player2, new MortalCombat());
        harness.setGraveyard(player2, IntStream.range(0, 20)
                .<Card>mapToObj(i -> new GrizzlyBears()).toList());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void controllerCanWinWithMortalCombat() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, IntStream.range(0, 20)
                .<Card>mapToObj(i -> new GrizzlyBears()).toList());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void controllerLosesWhenAngelDiesToLethalDamage() {
        var angel = harness.addToBattlefieldAndReturn(player1, new PlatinumAngel());
        harness.setLife(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, angel.getId());
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.castAndResolveInstant(player2, 0, angel.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(angel.getCard());
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
