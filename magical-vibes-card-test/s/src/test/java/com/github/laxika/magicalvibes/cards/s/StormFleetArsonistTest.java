package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormFleetArsonist.class, RaptorCompanion.class, Mountain.class})
class StormFleetArsonistTest extends BaseCardTest {

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
        harness.addToBattlefield(player2, new RaptorCompanion());
        markAttackedThisTurn();
        castStormFleetArsonist();

        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        // Opponent's only permanent should be auto-sacrificed
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("ETB raid trigger prompts opponent to choose when they have multiple permanents")
    void etbPromptsChoiceWithMultiplePermanents() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.addToBattlefield(player2, new RaptorCompanion());
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
        harness.assertInGraveyard(player2, "Raptor Companion");
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

    @Test
    @DisplayName("ETB does NOT trigger without raid (did not attack this turn)")
    void etbDoesNotTriggerWithoutRaid() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack and no target prompt (intervening-if failed)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Creature is on the battlefield
        harness.assertOnBattlefield(player1, "Storm Fleet Arsonist");

        // Opponent's permanent unchanged
        harness.assertOnBattlefield(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("An opponent attacking does not satisfy your raid condition")
    void opponentAttackDoesNotEnableRaid() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castStormFleetArsonist();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("Creature enters battlefield even without raid")
    void creatureEntersWithoutRaid() {
        castStormFleetArsonist();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Storm Fleet Arsonist");
    }

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

    @Test
    @DisplayName("Opponent can choose a land rather than a creature to sacrifice")
    void opponentCanSacrificeLand() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        markAttackedThisTurn();
        castStormFleetArsonist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Raptor Companion");
        harness.assertOnBattlefield(player1, "Storm Fleet Arsonist");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castStormFleetArsonist() {
        harness.castFromHand(player1, new StormFleetArsonist(), "{4}{R}");
    }
}
