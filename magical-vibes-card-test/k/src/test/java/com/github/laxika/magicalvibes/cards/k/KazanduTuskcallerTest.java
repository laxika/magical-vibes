package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KazanduTuskcaller.class})
class KazanduTuskcallerTest extends BaseCardTest {

    @Test
    @DisplayName("At levels two through five Kazandu Tuskcaller creates one Elephant token")
    void createsOneElephantAtLevelsTwoThroughFive() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 2);
        prepareForAbility(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("At level six Kazandu Tuskcaller creates two Elephant tokens")
    void createsTwoElephantsAtLevelSix() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 6);
        prepareForAbility(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(2);
    }

    @Test
    @DisplayName("Kazandu Tuskcaller has no token ability before level two")
    void hasNoTokenAbilityBeforeLevelTwo() {
        addCreatureReady(player1, new KazanduTuskcaller());
        prepareForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelUpPaysManaUsesStackAndDoesNotTap() {
        Permanent tuskcaller = harness.addToBattlefieldAndReturn(player1, new KazanduTuskcaller());
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(tuskcaller.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(tuskcaller.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(tuskcaller.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelUpRequiresOwnMainPhase() {
        addCreatureReady(player1, new KazanduTuskcaller());
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelingThroughThresholdsChangesTokenCount() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 1);
        prepareForAbility(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(tuskcaller.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);

        tuskcaller.setCounterCount(CounterType.LEVEL, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(tuskcaller.getCounterCount(CounterType.LEVEL)).isEqualTo(6);
        tuskcaller.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elephant")).isEqualTo(3);
    }

    @Test
    void tokenAbilityTracksUpperBoundaryAndRemovedCounters() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        prepareForAbility(player1);
        int expectedTokens = 0;
        for (int level : new int[]{5, 7, 5, 2}) {
            tuskcaller.setCounterCount(CounterType.LEVEL, level);
            tuskcaller.untap();
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
            expectedTokens += level >= 6 ? 2 : 1;
            assertThat(countPermanents(player1, "Elephant")).isEqualTo(expectedTokens);
        }
        tuskcaller.setCounterCount(CounterType.LEVEL, 1);
        tuskcaller.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenAbilityWorksDuringOpponentsTurnAndPaysTapCost() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(tuskcaller.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elephant")).isZero();
    }

    @Test
    void tokenAbilityCannotIgnoreSummoningSickness() {
        Permanent tuskcaller = harness.addToBattlefieldAndReturn(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 6);
        prepareForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Elephant")).isZero();
    }

    @Test
    void pendingTokenAbilityRetainsItsOriginalAmountAfterLevelsAreRemoved() {
        Permanent tuskcaller = addCreatureReady(player1, new KazanduTuskcaller());
        tuskcaller.setCounterCount(CounterType.LEVEL, 6);
        prepareForAbility(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        tuskcaller.setCounterCount(CounterType.LEVEL, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elephant")).isEqualTo(2);
    }

    private void prepareForAbility(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
