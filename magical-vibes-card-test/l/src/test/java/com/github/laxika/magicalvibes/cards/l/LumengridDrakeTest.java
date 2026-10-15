package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumengridDrake.class, AlphaTyrranax.class, Memnite.class})
class LumengridDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers when metalcraft is met (3+ artifacts) - target chosen at trigger time")
    void etbTriggersWithMetalcraft() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new AlphaTyrranax());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell - trigger-time target prompt

        // Casting never asked for a target - the prompt fires as the trigger goes on the stack
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Alpha Tyrranax"));

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lumengrid Drake");
    }

    @Test
    @DisplayName("ETB resolves: target creature is returned to owner's hand")
    void etbBouncesCreatureWithMetalcraft() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new AlphaTyrranax());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell - trigger-time target prompt
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Alpha Tyrranax"));
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Alpha Tyrranax");
        harness.assertInHand(player2, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("Drake enters the battlefield when metalcraft is met")
    void drakeEntersWithMetalcraft() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new AlphaTyrranax());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell - trigger-time target prompt
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Alpha Tyrranax"));
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Lumengrid Drake");
    }

    @Test
    @DisplayName("Stack is empty after full resolution with metalcraft")
    void stackEmptyAfterResolution() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new AlphaTyrranax());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell - trigger-time target prompt
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Alpha Tyrranax"));
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does NOT trigger without metalcraft (0 artifacts)")
    void etbDoesNotTriggerWithoutMetalcraft() {
        harness.addToBattlefield(player2, new AlphaTyrranax());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack and no target prompt (intervening-if condition failed)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Drake is still on the battlefield
        harness.assertOnBattlefield(player1, "Lumengrid Drake");

        // No creature was bounced
        harness.assertOnBattlefield(player2, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("ETB does NOT trigger with only 2 artifacts")
    void etbDoesNotTriggerWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new AlphaTyrranax());

        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger and no target prompt
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // No creature was bounced
        harness.assertOnBattlefield(player2, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("ETB does nothing if metalcraft is lost before resolution")
    void etbFizzlesWhenMetalcraftLost() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new AlphaTyrranax());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell - trigger-time target prompt
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Alpha Tyrranax"));

        // Remove artifacts before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Memnite"));

        harness.passBothPriorities(); // resolve ETB trigger - metalcraft no longer met

        // Target creature was NOT bounced
        harness.assertOnBattlefield(player2, "Alpha Tyrranax");

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("metalcraft ability does nothing"));
    }

    @Test
    @DisplayName("Can bounce own creature with metalcraft")
    void canBounceOwnCreature() {
        setupMetalcraft();
        harness.addToBattlefield(player1, new AlphaTyrranax());

        UUID targetId = harness.getPermanentId(player1, "Alpha Tyrranax");
        harness.castFromHand(player1, new LumengridDrake(), "{3}{U}");

        harness.passBothPriorities(); // resolve creature spell - trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Alpha Tyrranax");
        harness.assertInHand(player1, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("The Drake can return itself when metalcraft is met")
    void canBounceItself() {
        setupMetalcraft();
        castLumengridDrake();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Lumengrid Drake"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lumengrid Drake");
        harness.assertInHand(player1, "Lumengrid Drake");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's artifacts do not satisfy metalcraft")
    void opponentsArtifactsDoNotCount() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        castLumengridDrake();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Lumengrid Drake");
    }

    @Test
    @DisplayName("Returning an artifact creature checks metalcraft before the artifact leaves")
    void canBounceArtifactThatSatisfiesMetalcraft() {
        setupMetalcraft();
        UUID targetId = harness.getPermanentId(player1, "Memnite");
        castLumengridDrake();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Memnite");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(targetId));
        assertThat(gd.stack).isEmpty();
    }

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
    }

    private void castLumengridDrake() {
        harness.castFromHand(player1, new LumengridDrake(), "{3}{U}");
    }
}
