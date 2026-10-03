package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Avizoa.class})
class AvizoaTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives +2/+2 until end of turn")
    void pumpsUntilEndOfTurn() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        assertThat(avizoa.getEffectivePower()).isEqualTo(4);
        assertThat(avizoa.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(avizoa.getEffectivePower()).isEqualTo(2);
        assertThat(avizoa.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability queues a skip of the controller's next untap step")
    void queuesSkipNextUntapStep() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Queued skip leaves the controller's permanents tapped on their next turn")
    void skipsNextUntapStep() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());
        Permanent otherAvizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();
        avizoa.tap();
        otherAvizoa.tap();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(avizoa.isTapped()).isTrue();
        assertThat(otherAvizoa.isTapped()).isTrue();
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);

        advanceTurn();
        advanceTurn();
        assertThat(avizoa.isTapped()).isFalse();
        assertThat(otherAvizoa.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void onlyOncePerTurn() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(avizoa), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate again on a new turn")
    void canActivateAgainOnNewTurn() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        assertThat(avizoa.getEffectivePower()).isEqualTo(4);
        assertThat(avizoa.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped creatures with summoning sickness can activate the free ability")
    void canActivateWhileTappedWithSummoningSickness() {
        harness.addToBattlefield(player1, new Avizoa());
        Permanent avizoa = findPermanent(player1, "Avizoa");
        avizoa.setSummoningSick(true);
        avizoa.tap();

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);
        harness.passBothPriorities();

        assertThat(avizoa.isTapped()).isTrue();
        assertThat(avizoa.getEffectivePower()).isEqualTo(4);
        assertThat(avizoa.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        Permanent avizoa = addCreatureReady(player1, new Avizoa());

        harness.activateAbility(player1, battlefieldIndex(avizoa), null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(avizoa), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();

        assertThat(avizoa.getEffectivePower()).isEqualTo(4);
        assertThat(gd.skipNextUntapStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Separate copies can each activate and skip two successive untap steps")
    void separateCopiesQueueSeparateUntapSkips() {
        Permanent first = addCreatureReady(player1, new Avizoa());
        Permanent second = addCreatureReady(player1, new Avizoa());
        Permanent opponent = addCreatureReady(player2, new Avizoa());
        first.tap();
        second.tap();
        opponent.tap();

        harness.activateAbility(player1, battlefieldIndex(first), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(second), null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(opponent.isTapped()).isFalse();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
