package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FossilFindTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one card at random from graveyard to hand")
    void returnsOneCardFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new FossilFind()));
        harness.setHand(player1, List.of(new FossilFind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Exactly one of the three graveyard cards returns to hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Two remaining cards + Fossil Find itself go to graveyard after resolution.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Returns the only card when graveyard has one card")
    void returnsTheOnlyCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FossilFind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Only Fossil Find itself in graveyard after resolution.
        harness.assertInGraveyard(player1, "Fossil Find");
    }

    @Test
    @DisplayName("Does nothing when graveyard is empty")
    void doesNothingWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new FossilFind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
