package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gigapede.class, GrizzlyBears.class})
class GigapedeTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers during its controller's upkeep from the graveyard")
    void triggersDuringControllersUpkeep() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(gigapede.getId());
    }

    @Test
    @DisplayName("Discarding a card returns Gigapede to its owner's hand")
    void discardReturnsGigapedeToHand() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gigapede");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining keeps Gigapede in the graveyard")
    void decliningKeepsGigapedeInGraveyard() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Gigapede");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot return Gigapede without a card to discard")
    void cannotReturnWithoutCardToDiscard() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Gigapede");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does nothing if Gigapede leaves the graveyard before resolution")
    void doesNothingIfItLeavesGraveyardBeforeResolution() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(gigapede));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gigapede");
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

}
