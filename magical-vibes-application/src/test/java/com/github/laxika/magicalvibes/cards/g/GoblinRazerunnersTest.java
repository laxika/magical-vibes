package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinRazerunnersTest extends BaseCardTest {

    // ===== {1}{R}, Sacrifice a land: Put a +1/+1 counter on this creature. =====

    @Test
    @DisplayName("Activating sacrifices a land and puts a +1/+1 counter on this creature")
    void activatingSacrificesLandAndAddsCounter() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        // Only one land on the battlefield → auto-sacrificed as cost.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(razerunners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    // ===== End step: deal damage equal to +1/+1 counters to target player or planeswalker =====

    @Test
    @DisplayName("Deals damage equal to the number of +1/+1 counters to the chosen opponent")
    void dealsDamageEqualToCounters() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Any player is a legal target — controller may be chosen")
    void canTargetController() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN -> END_STEP, triggers fire
    }
}
