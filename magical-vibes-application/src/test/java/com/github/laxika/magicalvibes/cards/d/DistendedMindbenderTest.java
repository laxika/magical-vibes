package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistendedMindbenderTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger: discard a nonland MV≤3 and a MV≥4 from the opponent's hand")
    void discardsBothBands() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), // MV 2 nonland
                new Forest(),       // land — not choosable for first band
                new AirElemental()  // MV 5
        )));

        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve cast trigger → hand choice

        PendingInteraction.RevealedHandChoice first =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(first).isNotNull();
        assertThat(first.validIndices()).containsExactly(0); // only Bears for MV≤3 nonland
        harness.handleCardChosen(player1, 0);

        PendingInteraction.RevealedHandChoice second =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(second).isNotNull();
        // Hand after removing Bears: Forest (0), Air Elemental (1)
        assertThat(second.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Air Elemental");
    }

    @Test
    @DisplayName("Only one band present: discard just that card")
    void discardsOnlyMatchingBand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Shock(), new Forest())));

        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0); // Shock only; no MV≥4 follow-up
        assertThat(choice.followUpFilter()).isNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Only MV≥4 band present: skip first band and discard the high card")
    void skipsEmptyFirstBand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new AirElemental())));

        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1); // Air Elemental only
        assertThat(choice.followUpFilter()).isNull();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // MV 2
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, new ArrayList<>(List.of(new Shock())));
        harness.setHand(player1, List.of(new DistendedMindbender()));
        // Emerge {5}{B}{B} reduced by 2 → {3}{B}{B}
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0); // discard Shock
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Distended Mindbender"))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast trigger cannot target the controller")
    void castTriggerCannotTargetSelf() {
        harness.setHand(player1, List.of(new DistendedMindbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
