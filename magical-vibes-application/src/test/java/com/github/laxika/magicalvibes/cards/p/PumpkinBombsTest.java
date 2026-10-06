package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PumpkinBombs.class, GrizzlyBears.class})
class PumpkinBombsTest extends BaseCardTest {

    @Test
    @DisplayName("Pumpkin Bombs draws, adds a fuse counter, damages, and changes control")
    void resolvesAllAbilityEffects() {
        Permanent bombs = addReadyPumpkinBombs();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(bombs.getCounterCount(CounterType.FUSE)).isEqualTo(1);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Pumpkin Bombs");
        harness.assertOnBattlefield(player2, "Pumpkin Bombs");
    }

    @Test
    @DisplayName("Pumpkin Bombs damage scales with its fuse counters")
    void damageScalesWithFuseCounters() {
        Permanent bombs = addReadyPumpkinBombs();
        bombs.setCounterCount(CounterType.FUSE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(bombs.getCounterCount(CounterType.FUSE)).isEqualTo(2);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Pumpkin Bombs requires two cards and an opponent target")
    void requiresDiscardCardsAndOpponentTarget() {
        addReadyPumpkinBombs();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A newly controlled noncreature artifact can activate and pays costs before resolution")
    void paysTapAndDiscardCostsBeforeResolving() {
        Permanent bombs = addReadyPumpkinBombs();
        bombs.setSummoningSick(true);
        harness.setHand(player1, List.of(new PumpkinBombs(), new PumpkinBombs()));
        harness.setLibrary(player1, List.of(new PumpkinBombs(), new PumpkinBombs(), new PumpkinBombs()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(bombs.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(bombs.getCounterCount(CounterType.FUSE)).isZero();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Pumpkin Bombs");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(bombs.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Pumpkin Bombs");
    }

    @Test
    @DisplayName("A tapped Pumpkin Bombs cannot activate")
    void cannotActivateWhileTapped() {
        Permanent bombs = addReadyPumpkinBombs();
        bombs.tap();
        harness.setHand(player1, List.of(new PumpkinBombs(), new PumpkinBombs()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(bombs.getCounterCount(CounterType.FUSE)).isZero();
    }

    @Test
    @DisplayName("The new controller draws and can send Pumpkin Bombs back with another fuse counter")
    void newControllerCanActivateAndReturnControl() {
        Permanent bombs = addReadyPumpkinBombs();
        harness.setHand(player1, List.of(new PumpkinBombs(), new PumpkinBombs()));
        harness.setLibrary(player1, List.of(new PumpkinBombs(), new PumpkinBombs(), new PumpkinBombs()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        bombs.untap();
        harness.setHand(player2, List.of(new PumpkinBombs(), new PumpkinBombs()));
        harness.setLibrary(player2, List.of(new PumpkinBombs(), new PumpkinBombs(), new PumpkinBombs()));

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(bombs.getCounterCount(CounterType.FUSE)).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Pumpkin Bombs");
        harness.assertNotOnBattlefield(player2, "Pumpkin Bombs");
    }

    @Test
    @DisplayName("Removing Pumpkin Bombs in response still draws and deals damage using its last fuse count")
    void removedSourceUsesLastKnownFuseCounters() {
        Permanent bombs = addReadyPumpkinBombs();
        bombs.setCounterCount(CounterType.FUSE, 2);
        harness.setHand(player1, List.of(new PumpkinBombs(), new PumpkinBombs()));
        harness.setLibrary(player1, List.of(new PumpkinBombs(), new PumpkinBombs(), new PumpkinBombs()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bombs));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Pumpkin Bombs");
        harness.assertNotOnBattlefield(player2, "Pumpkin Bombs");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private Permanent addReadyPumpkinBombs() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bombs = harness.addToBattlefieldAndReturn(player1, new PumpkinBombs());
        bombs.setSummoningSick(false);
        return bombs;
    }
}
