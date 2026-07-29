package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulEchoTest extends BaseCardTest {

    /**
     * Puts a Soul Echo with {@code counters} echo counters onto player1's battlefield, advances to
     * player1's upkeep, targets player2 with the trigger and resolves it.
     */
    private Permanent echoAtUpkeep(int counters) {
        harness.addToBattlefield(player1, new SoulEcho());
        Permanent echo = findPermanent(player1, "Soul Echo");
        echo.setCounterCount(CounterType.ECHO, counters);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        return echo;
    }

    /** player2 bolts player1 for 3 and lets it resolve. */
    private void boltController() {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting Soul Echo with X=4 enters with 4 echo counters")
    void entersWithXEchoCounters() {
        harness.setHand(player1, List.of(new SoulEcho()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Soul Echo").getCounterCount(CounterType.ECHO)).isEqualTo(4);
    }

    @Test
    @DisplayName("The upkeep trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new SoulEcho());
        findPermanent(player1, "Soul Echo").setCounterCount(CounterType.ECHO, 2);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("With no echo counters the upkeep trigger sacrifices Soul Echo and prompts nobody")
    void sacrificedWithNoEchoCounters() {
        echoAtUpkeep(0);

        harness.assertNotOnBattlefield(player1, "Soul Echo");
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Accepting replaces each 1 damage to the controller with an echo counter removal")
    void acceptingReplacesDamageWithCounterRemoval() {
        Permanent echo = echoAtUpkeep(5);
        harness.setLife(player1, 20);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        boltController();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(echo.getCounterCount(CounterType.ECHO)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining leaves damage to the controller dealt normally")
    void decliningLeavesDamageNormal() {
        Permanent echo = echoAtUpkeep(5);
        harness.setLife(player1, 20);

        harness.handleMayAbilityChosen(player2, false);

        boltController();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(echo.getCounterCount(CounterType.ECHO)).isEqualTo(5);
    }

    @Test
    @DisplayName("Damage beyond the remaining echo counters is dealt normally")
    void excessDamageIsDealtNormally() {
        Permanent echo = echoAtUpkeep(1);
        harness.setLife(player1, 20);

        harness.handleMayAbilityChosen(player2, true);

        boltController();

        assertThat(echo.getCounterCount(CounterType.ECHO)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The controller does not lose the game at 0 or less life")
    void controllerDoesNotLoseAtZeroLife() {
        echoAtUpkeep(2);
        harness.handleMayAbilityChosen(player2, false);
        harness.setLife(player1, 1);

        boltController();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-2);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
