package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.n.NobleElephant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulEcho.class, Incinerate.class, NobleElephant.class})
class SoulEchoTest extends BaseCardTest {

    /**
     * Puts a Soul Echo with {@code counters} echo counters onto player1's battlefield, advances to
     * player1's upkeep, targets player2 with the trigger and resolves it.
     */
    private Permanent echoAtUpkeep(int counters) {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new SoulEcho());
        echo.setCounterCount(CounterType.ECHO, counters);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        return echo;
    }

    /** Player2 casts Incinerate at player1 and lets it resolve. */
    private void incinerateController() {
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
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
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new SoulEcho());
        echo.setCounterCount(CounterType.ECHO, 2);

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

        incinerateController();

        harness.assertLife(player1, 20);
        assertThat(echo.getCounterCount(CounterType.ECHO)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining leaves damage to the controller dealt normally")
    void decliningLeavesDamageNormal() {
        Permanent echo = echoAtUpkeep(5);
        harness.setLife(player1, 20);

        harness.handleMayAbilityChosen(player2, false);

        incinerateController();

        harness.assertLife(player1, 17);
        assertThat(echo.getCounterCount(CounterType.ECHO)).isEqualTo(5);
    }

    @Test
    @DisplayName("Damage remains replaced after the echo counters run out")
    void damageRemainsReplacedAfterCountersRunOut() {
        Permanent echo = echoAtUpkeep(1);
        harness.setLife(player1, 20);

        harness.handleMayAbilityChosen(player2, true);

        incinerateController();

        assertThat(echo.getCounterCount(CounterType.ECHO)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage before the first upkeep choice is dealt normally")
    void damageBeforeFirstUpkeepChoiceIsDealtNormally() {
        Permanent echo = harness.addToBattlefieldAndReturn(player1, new SoulEcho());
        echo.setCounterCount(CounterType.ECHO, 2);
        harness.setLife(player1, 20);

        incinerateController();

        harness.assertLife(player1, 17);
        assertThat(echo.getCounterCount(CounterType.ECHO)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage at the next upkeep before its trigger resolves is dealt normally")
    void damageAtNextUpkeepBeforeTriggerResolvesIsDealtNormally() {
        Permanent echo = echoAtUpkeep(2);
        harness.handleMayAbilityChosen(player2, true);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player1, 20);

        incinerateController();

        harness.assertLife(player1, 17);
        assertThat(echo.getCounterCount(CounterType.ECHO)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Soul Echoes do not split one damage event between enchantments")
    void multipleSoulEchoesDoNotSplitDamageEvent() {
        Permanent firstEcho = harness.addToBattlefieldAndReturn(player1, new SoulEcho());
        Permanent secondEcho = harness.addToBattlefieldAndReturn(player1, new SoulEcho());
        firstEcho.setCounterCount(CounterType.ECHO, 1);
        secondEcho.setCounterCount(CounterType.ECHO, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        if (gd.pendingMayAbilities.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player2, true);

        harness.setLife(player1, 20);
        incinerateController();

        harness.assertLife(player1, 19);
        assertThat(firstEcho.getCounterCount(CounterType.ECHO)
                + secondEcho.getCounterCount(CounterType.ECHO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepted replacement also applies to combat damage")
    void acceptedReplacementAppliesToCombatDamage() {
        Permanent echo = echoAtUpkeep(2);
        harness.handleMayAbilityChosen(player2, true);
        harness.setLife(player1, 20);
        addCreatureReady(player2, new NobleElephant());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(echo.getCounterCount(CounterType.ECHO)).isZero();
    }

    @Test
    @DisplayName("The controller does not lose the game at 0 or less life")
    void controllerDoesNotLoseAtZeroLife() {
        echoAtUpkeep(2);
        harness.handleMayAbilityChosen(player2, false);
        harness.setLife(player1, 1);

        incinerateController();

        harness.assertLife(player1, -2);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
