package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForgetTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Forget targets a player")
    void castingTargetsPlayer() {
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Target discards two chosen cards, then draws two")
    void discardsTwoThenDrawsTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Peek())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island(), new Island())));
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player (not the caster) chooses which two to discard.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0); // discard Grizzly Bears
        harness.handleCardChosen(player2, 0); // discard Peek

        // Discarded two, then drew two — hand is back to three, both Islands drawn.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(c -> c.getName().equals("Island")).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Target holding one card discards it and draws only one")
    void oneCardDiscardsOneDrawsOne() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island(), new Island())));
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0); // discard the only card

        // Discarded one, so draws only one (not two).
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Island");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Target with empty hand discards and draws nothing")
    void emptyHandDrawsNothing() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island(), new Island())));
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
