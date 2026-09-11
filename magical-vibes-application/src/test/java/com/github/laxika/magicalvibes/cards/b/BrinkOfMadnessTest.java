package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrinkOfMadness.class, GiantCockroach.class, YavimayaWurm.class})
class BrinkOfMadnessTest extends BaseCardTest {

    @Test
    @DisplayName("With an empty hand, the upkeep trigger sacrifices Brink of Madness and discards the target opponent's hand")
    void emptyHandSacrificesAndDiscardsTargetOpponentHand() {
        harness.addToBattlefield(player1, new BrinkOfMadness());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GiantCockroach(), new YavimayaWurm()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brink of Madness");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Giant Cockroach", "Yavimaya Wurm");
    }

    @Test
    @DisplayName("Brink of Madness does not trigger while its controller has cards in hand")
    void doesNotTriggerWithCardsInHand() {
        harness.addToBattlefield(player1, new BrinkOfMadness());
        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.setHand(player2, List.of(new YavimayaWurm()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Brink of Madness");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Yavimaya Wurm");
    }

    @Test
    @DisplayName("The empty-hand condition is checked again when the trigger resolves")
    void conditionIsRecheckedOnResolution() {
        harness.addToBattlefield(player1, new BrinkOfMadness());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new YavimayaWurm()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, List.of(new GiantCockroach()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brink of Madness");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Yavimaya Wurm");
    }

    @Test
    @DisplayName("Brink of Madness triggers only during its controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BrinkOfMadness());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new YavimayaWurm()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Brink of Madness");
    }

    @Test
    @DisplayName("The upkeep trigger cannot target its controller")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new BrinkOfMadness());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
