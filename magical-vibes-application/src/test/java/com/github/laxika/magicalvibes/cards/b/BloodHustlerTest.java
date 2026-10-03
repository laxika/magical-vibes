package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed(BloodHustler.class)
class BloodHustlerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller commits a crime")
    void putsCounterOnCrime() {
        Permanent hustler = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateDrain(hustler);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hustler)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        Permanent hustler = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        activateDrain(hustler);
        resolveAllTriggers();
        activateDrain(hustler);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hustler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target yourself with the drain ability")
    void cannotTargetYourself() {
        harness.addToBattlefield(player1, new BloodHustler());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crime counter resolves before the drain ability")
    void counterResolvesBeforeDrain() {
        Permanent hustler = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        activateDrain(hustler);
        assertThat(hustler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(hustler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
    }

    @Test
    @DisplayName("A second crime while the first trigger is pending does not trigger again")
    void pendingTriggerStillCountsForTurnLimit() {
        Permanent hustler = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        activateDrain(hustler);
        activateDrain(hustler);
        resolveAllTriggers();

        assertThat(hustler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 2);
    }

    @Test
    @DisplayName("The crime trigger resets on the opponent's turn and the drain works while tapped")
    void triggersAgainOnOpponentsTurnWhileTapped() {
        Permanent hustler = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        activateDrain(hustler);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        hustler.setTapped(true);
        int opponentLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        activateDrain(hustler);
        resolveAllTriggers();

        assertThat(hustler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(hustler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each controlled Hustler triggers independently, but opposing Hustlers do not")
    void crimeTriggersOnlyControllersHustlers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BloodHustler());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new BloodHustler());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        activateDrain(first);
        resolveAllTriggers();
        activateDrain(second);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    private void activateDrain(Permanent hustler) {
        harness.activateAbility(player1, indexOf(player1, hustler), null, player2.getId());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
