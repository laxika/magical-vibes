package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagManTest extends BaseCardTest {

    private void readyRagMan() {
        addCreatureReady(player1, new RagMan());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Discards the only creature card from target opponent's hand")
    void discardsCreatureAtRandom() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears(), new Peek())));
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Only creature in hand — deterministically discarded, non-creatures untouched.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Only ever discards a creature card, never a noncreature")
    void onlyDiscardsCreatures() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Peek(), new GrizzlyBears())));
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Whichever creature is picked, the noncreature cards must all remain in hand.
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .allMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing when the opponent has no creature cards")
    void noCreatureNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Peek())));
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Handles an empty hand gracefully")
    void emptyHandNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        readyRagMan();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated during the opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addCreatureReady(player1, new RagMan());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
