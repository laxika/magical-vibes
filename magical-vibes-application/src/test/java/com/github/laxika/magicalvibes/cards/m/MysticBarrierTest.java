package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticBarrier.class, AjanisPridemate.class, JaceBeleren.class})
class MysticBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB direction limits attacks to the nearest opponent in that direction")
    void etbDirectionLimitsAttackTargets() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player1, new AjanisPridemate());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.enterBattlefieldAndReturn(player1, new MysticBarrier());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        harness.passBothPriorities();

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, planeswalker.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    @Test
    @DisplayName("The entrance trigger permits responses before the direction is chosen")
    void entranceChoiceWaitsForTriggerResolution() {
        harness.castFromHand(player1, new MysticBarrier());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Left");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Left applies to every player's attacks and stops applying when Barrier leaves")
    void leftRestrictionIsGlobalAndEndsWhenSourceLeaves() {
        Permanent firstAttacker = addCreatureReady(player1, new AjanisPridemate());
        Permanent secondAttacker = addCreatureReady(player2, new AjanisPridemate());
        Permanent barrier = enterAndChooseDirection("Left");
        UUID player3Id = addThirdPlayer();

        assertThat(als.canAttackDefender(gd, firstAttacker, player3Id)).isTrue();
        assertThat(als.canAttackDefender(gd, firstAttacker, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, secondAttacker, player1.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, secondAttacker, player3Id)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(barrier);

        assertThat(als.canAttackDefender(gd, firstAttacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, secondAttacker, player3Id)).isTrue();
    }

    @Test
    @DisplayName("The controller can change the direction during upkeep")
    void upkeepChangesDirection() {
        Permanent attacker = addCreatureReady(player1, new AjanisPridemate());
        enterAndChooseDirection("Right");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Left");
        resolveAllTriggers();
        UUID player3Id = addThirdPlayer();

        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("An opponent's upkeep does not offer a new direction choice")
    void opponentUpkeepDoesNotChangeDirection() {
        Permanent attacker = addCreatureReady(player1, new AjanisPridemate());
        enterAndChooseDirection("Right");

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        UUID player3Id = addThirdPlayer();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    @Test
    @DisplayName("Opposite directions allow attacking in a duel but prevent attacks with three players")
    void oppositeDirectionsAreIndependentRestrictions() {
        Permanent attacker = addCreatureReady(player1, new AjanisPridemate());
        enterAndChooseDirection("Left");
        enterAndChooseDirection("Right");

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        UUID player3Id = addThirdPlayer();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    private Permanent enterAndChooseDirection(String direction) {
        Permanent barrier = harness.enterBattlefieldAndReturn(player1, new MysticBarrier());
        harness.passBothPriorities();
        harness.handleListChoice(player1, direction);
        resolveAllTriggers();
        return barrier;
    }

    private UUID addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        return player3Id;
    }
}
