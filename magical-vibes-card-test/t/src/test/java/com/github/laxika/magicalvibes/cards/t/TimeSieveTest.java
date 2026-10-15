package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SilverMyr;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimeSieve.class, Spellbook.class, LeoninScimitar.class, SilverMyr.class, GoldMyr.class})
class TimeSieveTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    /** Battlefield: Time Sieve + exactly four other artifacts (five artifacts total). */
    private void setupFiveArtifacts() {
        harness.addToBattlefield(player1, new TimeSieve());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Activating auto-sacrifices all five artifacts and puts the ability on the stack")
    void activatingSacrificesFiveArtifacts() {
        setupFiveArtifacts();

        harness.activateAbility(player1, 0, null, null);

        // Exactly five artifacts -> all auto-sacrificed (including Time Sieve itself)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving queues an extra turn for the controller")
    void resolvingQueuesExtraTurn() {
        setupFiveArtifacts();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.extraTurns).containsExactly(player1.getId());
        });
    }

    @Test
    @DisplayName("The queued extra turn is taken by the controller after the current turn ends")
    void extraTurnTaken() {
        setupFiveArtifacts();
        int turnBefore = gd.turnNumber;
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            advanceTurn();

            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
            assertThat(gd.extraTurns).isEmpty();
        });
    }

    @Test
    @DisplayName("Cannot activate with fewer than five artifacts")
    void cannotActivateWithoutFiveArtifacts() {
        harness.addToBattlefield(player1, new TimeSieve());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new SilverMyr());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Only four artifacts total (Time Sieve + 3)
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate when Time Sieve is already tapped")
    void cannotActivateWhenTapped() {
        setupFiveArtifacts();
        findPermanent(player1, "Time Sieve").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can sacrifice five other artifacts and keep the tapped Time Sieve")
    void canKeepTimeSieveWhenChoosingSacrifices() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new TimeSieve());
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        List<Permanent> artifacts = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        Permanent source = artifacts.getFirst();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            for (Permanent artifact : artifacts.subList(1, 6)) {
                harness.handlePermanentChosen(player1, artifact.getId());
            }

            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
            assertThat(source.isTapped()).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
            assertThat(gd.extraTurns).isEmpty();
            harness.passBothPriorities();
            assertThat(gd.extraTurns).containsExactly(player1.getId());
        });
    }

    @Test
    @DisplayName("Opponent's artifacts cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifacts() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new TimeSieve());
        }
        harness.addToBattlefield(player2, new TimeSieve());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An activation on the opponent's turn inserts an extra turn before the normal turn")
    void extraTurnDuringOpponentsTurnPreservesNormalTurnOrder() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new TimeSieve());
        }
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int turnBefore = gd.turnNumber;

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.extraTurns).containsExactly(player1.getId());

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
            assertThat(gd.extraTurns).isEmpty();

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);

            advanceTurn();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 3);
        });
    }
}
