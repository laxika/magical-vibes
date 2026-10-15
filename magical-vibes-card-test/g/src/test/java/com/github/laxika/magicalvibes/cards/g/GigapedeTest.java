package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gigapede.class, Forest.class, Shock.class})
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
        harness.setHand(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gigapede");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining keeps Gigapede in the graveyard")
    void decliningKeepsGigapedeInGraveyard() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));
        harness.setHand(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Gigapede");
        harness.assertInHand(player1, "Forest");
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
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gigapede");
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

    @Test
    @DisplayName("Returning Gigapede is part of the same resolution as the discard")
    void returnsImmediatelyAfterDiscard() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));
        harness.setHand(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Gigapede");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An old upkeep trigger cannot act on Gigapede after it leaves and re-enters the graveyard")
    void leavingAndReenteringGraveyardInvalidatesTrigger() {
        Gigapede gigapede = new Gigapede();
        harness.setGraveyard(player1, List.of(gigapede));
        gd.markGraveyardEntry(gigapede);
        harness.setHand(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Forest(), gigapede));
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(gigapede));
        gd.markGraveyardEntry(gigapede);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gigapede");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gigapede on the battlefield does not trigger its graveyard upkeep ability")
    void doesNotTriggerFromBattlefield() {
        harness.addToBattlefield(player1, new Gigapede());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
    @Test
    @DisplayName("Shroud prevents its controller from targeting Gigapede")
    void controllerCannotTargetGigapede() {
        var gigapede = harness.addToBattlefieldAndReturn(player1, new Gigapede());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, gigapede.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents an opponent from targeting Gigapede")
    void opponentCannotTargetGigapede() {
        var gigapede = harness.addToBattlefieldAndReturn(player2, new Gigapede());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, gigapede.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
