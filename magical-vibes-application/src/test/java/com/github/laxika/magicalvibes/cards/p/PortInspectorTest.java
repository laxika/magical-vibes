package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PortInspector.class, FreshVolunteers.class})
class PortInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When blocked, accepting the trigger lets its controller look at the defending player's hand")
    void acceptingBlockedTriggerLooksAtDefendingPlayersHand() {
        declarePortInspectorBlocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Fresh Volunteers"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
    }

    @Test
    @DisplayName("When blocked, declining the trigger does not reveal the defending player's hand")
    void decliningBlockedTriggerDoesNotLookAtHand() {
        declarePortInspectorBlocked();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
    }

    @Test
    @DisplayName("When blocked by multiple creatures, it offers one hand-look trigger")
    void multipleBlockersOfferOneHandLookTrigger() {
        declarePortInspectorBlocked(List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).hasSize(1);
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
    }

    @Test
    @DisplayName("When unblocked, it does not offer a hand-look trigger")
    void unblockedDoesNotLookAtHand() {
        Permanent inspector = addCreatureReady(player1, new PortInspector());
        inspector.setAttacking(true);
        inspector.setAttackTarget(player2.getId());
        harness.setHand(player2, List.of(new FreshVolunteers()));

        resolveCombat();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void declarePortInspectorBlocked() {
        declarePortInspectorBlocked(List.of(new BlockerAssignment(0, 0)));
    }

    private void declarePortInspectorBlocked(List<BlockerAssignment> assignments) {
        Permanent inspector = addCreatureReady(player1, new PortInspector());
        inspector.setAttacking(true);
        inspector.setAttackTarget(player2.getId());

        int blockerCount = assignments.stream()
                .mapToInt(BlockerAssignment::blockerIndex)
                .max()
                .orElseThrow() + 1;
        for (int i = 0; i < blockerCount; i++) {
            addCreatureReady(player2, new FreshVolunteers());
        }
        harness.setHand(player2, List.of(new FreshVolunteers()));

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }
}
