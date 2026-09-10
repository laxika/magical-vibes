package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Allay;
import com.github.laxika.magicalvibes.cards.f.Forbid;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scrivener.class, Allay.class, Forbid.class, RagingGoblin.class})
class ScrivenerTest extends BaseCardTest {

    private void castScrivener() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Scrivener(), "{4}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns the chosen instant card from the graveyard")
    void returnsChosenInstantToHand() {
        Allay allay = new Allay();
        Forbid forbid = new Forbid();
        harness.setGraveyard(player1, List.of(new RagingGoblin(), allay, forbid));

        castScrivener();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(allay.getId(), forbid.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forbid.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forbid");
        harness.assertInGraveyard(player1, "Allay");
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("The optional return may be declined")
    void returnMayBeDeclined() {
        Forbid forbid = new Forbid();
        harness.setGraveyard(player1, List.of(forbid));

        castScrivener();

        harness.handleMultipleCardsChosen(player1, List.of(forbid.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Forbid");
        harness.assertNotInHand(player1, "Forbid");
    }

    @Test
    @DisplayName("A non-instant card is not a legal target")
    void nonInstantIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new RagingGoblin()));

        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("An empty graveyard produces no target choice")
    void emptyGraveyardProducesNoChoice() {
        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("An instant in an opponent's graveyard is not a legal target")
    void opponentGraveyardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new Forbid()));

        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Forbid");
    }

    @Test
    @DisplayName("A targeted instant that leaves the graveyard is not returned")
    void targetLeavingGraveyardIsNotReturned() {
        Forbid forbid = new Forbid();
        harness.setGraveyard(player1, List.of(forbid));

        castScrivener();

        harness.handleMultipleCardsChosen(player1, List.of(forbid.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forbid");
    }
}
