package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmokestackTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may add a soot counter during their upkeep")
    void controllerMayAddSootCounter() {
        Permanent smokestack = addSmokestack(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(smokestack.getCounterCount(CounterType.SOOT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the soot counter does not add one")
    void controllerMayDeclineSootCounter() {
        Permanent smokestack = addSmokestack(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(smokestack.getCounterCount(CounterType.SOOT)).isZero();
    }

    @Test
    @DisplayName("Each player sacrifices one permanent per soot counter during their upkeep")
    void eachPlayerSacrificesPerSootCounter() {
        Permanent smokestack = addSmokestack(player1);
        smokestack.setCounterCount(CounterType.SOOT, 2);
        Permanent player1Permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1PermanentTwo = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2PermanentTwo = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        choosePermanents(player2, player2Permanent, player2PermanentTwo);

        assertThat(battlefield(player2)).doesNotContain(player2Permanent, player2PermanentTwo);
        assertThat(battlefield(player1)).contains(player1Permanent, player1PermanentTwo);
    }

    @Test
    @DisplayName("A player sacrifices all available permanents when they have fewer than the soot count")
    void sacrificesAllAvailablePermanents() {
        Permanent smokestack = addSmokestack(player1);
        smokestack.setCounterCount(CounterType.SOOT, 3);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(battlefield(player2)).doesNotContain(permanent);
    }

    private Permanent addSmokestack(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new Smokestack());
    }

    private void choosePermanents(Player player, Permanent... permanents) {
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player,
                java.util.Arrays.stream(permanents).map(Permanent::getId).toList());
    }

    private java.util.List<Permanent> battlefield(Player player) {
        return gd.playerBattlefields.get(player.getId());
    }
}
