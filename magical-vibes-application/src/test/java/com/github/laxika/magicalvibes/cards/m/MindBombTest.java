package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindBombTest extends BaseCardTest {

    @Test
    @DisplayName("Each player takes 3 minus the number of cards they chose to discard")
    void eachPlayerTakesThreeMinusDiscarded() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new MindBomb(), new GrizzlyBears(), new HillGiant()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // active player chooses first (APNAP)

        // Player 1 discards 1 of its 2 remaining cards -> takes 3 - 1 = 2 damage.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0); // Grizzly Bears

        // Player 2 keeps its hand -> takes the full 3 damage.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Discarding three cards takes no damage; an empty-handed player takes the full three")
    void discardThreeTakesNoDamageEmptyHandTakesThree() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new MindBomb(), new GrizzlyBears(), new HillGiant(), new AirElemental()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player 1 discards all three remaining cards -> takes 3 - 3 = 0 damage.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 3);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        // Player 2 has no cards, so no choice is offered; it simply takes the full 3.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears", "Hill Giant", "Air Elemental");
    }
}
