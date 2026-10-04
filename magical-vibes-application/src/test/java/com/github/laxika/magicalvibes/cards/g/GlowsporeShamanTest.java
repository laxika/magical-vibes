package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlowsporeShaman.class, Forest.class, GrizzlyBears.class})
class GlowsporeShamanTest extends BaseCardTest {

    private void castAndResolveToMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GlowsporeShaman(), "{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB mills three cards then prompts may put a land on top")
    void etbMillsThenMayPrompt() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertOnBattlefield(player1, "Glowspore Shaman");
    }

    @Test
    @DisplayName("Accepting may puts a milled land on top of the library")
    void acceptingMayPutsMilledLandOnTop() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining may leaves milled cards in the graveyard")
    void decliningMayLeavesCardsInGraveyard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot choose a nonland from the graveyard")
    void cannotChooseNonland() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    void canReturnLandAlreadyInGraveyardAboveUnmilledCards() {
        Forest land = new Forest();
        GrizzlyBears remainingCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), remainingCard));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).doesNotContain(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsAndStillCanReturnLand() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castAndResolveToMay();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void acceptingWithNoLandFinishesWithoutGraveyardChoice() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
