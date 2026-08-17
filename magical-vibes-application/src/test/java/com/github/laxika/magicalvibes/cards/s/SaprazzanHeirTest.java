package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaprazzanHeirTest extends BaseCardTest {

    @Test
    @DisplayName("When Saprazzan Heir becomes blocked, accepting the trigger draws three cards")
    void acceptingTriggerDrawsThreeCards() {
        Permanent heir = addHeir();
        heir.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareBlock();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Declining Saprazzan Heir's trigger does not draw cards")
    void decliningTriggerDrawsNothing() {
        Permanent heir = addHeir();
        heir.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareBlock();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Saprazzan Heir triggers once when blocked by multiple creatures")
    void multipleBlockersCreateOneTrigger() {
        Permanent heir = addHeir();
        heir.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareBlock(List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("An unblocked Saprazzan Heir does not create a draw trigger")
    void unblockedDoesNotTrigger() {
        Permanent heir = addHeir();
        heir.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addHeir() {
        return addCreatureReady(player1, new SaprazzanHeir());
    }

    private void declareBlock() {
        declareBlock(List.of(new BlockerAssignment(0, 0)));
    }

    private void declareBlock(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
    }
}
