package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BojukaBrigand.class, GrizzlyBears.class})
class BojukaBrigandTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may put a +1/+1 counter on it")
    void ownAllyEntryMayPutCounterOnIt() {
        harness.setHand(player1, List.of(new BojukaBrigand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent brigand = findPermanent(player1, "Bojuka Brigand");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(brigand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Ally entering triggers both Bojuka Brigands")
    void anotherAllyEntryTriggersBothBrigands() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new BojukaBrigand());
        harness.setHand(player1, List.of(new BojukaBrigand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent brigand = harness.addToBattlefieldAndReturn(player1, new BojukaBrigand());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(brigand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void mayBeDeclined() {
        harness.setHand(player1, List.of(new BojukaBrigand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Bojuka Brigand")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bojuka Brigand cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new BojukaBrigand());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger your Brigand")
    void opponentAllyDoesNotTrigger() {
        Permanent ownBrigand = harness.addToBattlefieldAndReturn(player1, new BojukaBrigand());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BojukaBrigand()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(ownBrigand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Bojuka Brigand")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Successive Ally entries each add a counter to the existing Brigand")
    void successiveAllyEntriesAccumulateCounters() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new BojukaBrigand());
        harness.setHand(player1, List.of(new BojukaBrigand(), new BojukaBrigand()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int entry = 0; entry < 2; entry++) {
            harness.castCreature(player1, 0);
            resolveAllTriggers();
            for (int trigger = 0; trigger < entry + 2; trigger++) {
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
                harness.handleMayAbilityChosen(player1, true);
                resolveAllTriggers();
            }
            assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(entry + 1);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }
}
