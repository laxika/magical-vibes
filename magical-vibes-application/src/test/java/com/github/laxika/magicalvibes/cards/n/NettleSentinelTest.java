package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NettleSentinelTest extends BaseCardTest {

    // ===== Static: doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Nettle Sentinel does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        harness.addToBattlefield(player1, new NettleSentinel());
        Permanent sentinel = findPermanent(player1, "Nettle Sentinel");
        sentinel.tap();

        advanceToNextTurn(player2);

        assertThat(sentinel.isTapped()).isTrue();
    }

    // ===== Trigger: whenever you cast a green spell =====

    @Test
    @DisplayName("Casting a green spell triggers the may untap prompt")
    void greenSpellTriggersMayPrompt() {
        harness.addToBattlefield(player1, new NettleSentinel());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting untaps a tapped Nettle Sentinel")
    void acceptUntapsSentinel() {
        harness.addToBattlefield(player1, new NettleSentinel());
        Permanent sentinel = findPermanent(player1, "Nettle Sentinel");
        sentinel.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves Nettle Sentinel tapped")
    void declineLeavesTapped() {
        harness.addToBattlefield(player1, new NettleSentinel());
        Permanent sentinel = findPermanent(player1, "Nettle Sentinel");
        sentinel.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Nettle Sentinel"));
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-green spell does not trigger Nettle Sentinel")
    void nonGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new NettleSentinel());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
