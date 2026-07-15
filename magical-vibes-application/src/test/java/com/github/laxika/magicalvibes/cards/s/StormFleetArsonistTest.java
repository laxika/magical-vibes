package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormFleetArsonistTest extends BaseCardTest {

    // ===== ETB with raid met =====

    @Test
    @DisplayName("ETB triggers sacrifice when raid is met (attacked this turn)")
    void etbTriggersWithRaid() {
        markAttackedThisTurn();
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Storm Fleet Arsonist");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB raid trigger makes target opponent sacrifice their only permanent")
    void etbMakesOpponentSacrificeOnlyPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castStormFleetArsonist();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        // Opponent's only permanent should be auto-sacrificed
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB raid trigger prompts opponent to choose when they have multiple permanents")
    void etbPromptsChoiceWithMultiplePermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castStormFleetArsonist();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        // Opponent should be prompted to choose which permanent to sacrifice
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        // Player2 chooses the first permanent
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        UUID chosen = p2Battlefield.getFirst().getId();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen));

        // One sacrificed, one remains
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB raid trigger does nothing when opponent has no permanents")
    void etbDoesNothingWithNoPermanents() {
        markAttackedThisTurn();
        castStormFleetArsonist();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no permanents to sacrifice"));
    }

    // ===== ETB without raid =====

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack and no target prompt (intervening-if failed, CR 603.4)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Creature is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Arsonist"));

        // Opponent's permanent unchanged
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Raid lost before resolution (intervening-if) =====

    @Test
    @DisplayName("ETB does nothing if raid condition is lost before resolution")
    void etbFizzlesWhenRaidLost() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId()); // ETB trigger on stack

        // Remove the raid flag before ETB resolves
        gd.playersDeclaredAttackersThisTurn.clear();

        harness.passBothPriorities(); // resolve ETB trigger — raid no longer met

        // Opponent's permanent should still be there
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("raid ability does nothing"));
    }

    // ===== Creature enters battlefield regardless =====

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Storm Fleet Arsonist"));
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Trigger target prompt only offers opponents — choosing yourself is rejected")
    void cannotTargetYourself() {
        markAttackedThisTurn();
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castStormFleetArsonist() {
        harness.setHand(player1, List.of(new StormFleetArsonist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }
}
