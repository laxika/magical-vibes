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

    private Permanent addReadyPumpkinBombs() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent bombs = harness.addToBattlefieldAndReturn(player1, new PumpkinBombs());
        bombs.setSummoningSick(false);
        return bombs;
    }
}
