package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.s.StonybrookBanneret;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightshadeSchemers.class, BallyrushBanneret.class, StonybrookBanneret.class})
class NightshadeSchemersTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of(new NightshadeSchemers())); // Faerie Wizard — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing the shared-type card makes each opponent lose 2 life")
    void revealDrainsOpponent() {
        addCreatureReady(player1, new NightshadeSchemers());
        NightshadeSchemers topCard = new NightshadeSchemers();
        harness.setLibrary(player1, List.of(topCard));

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Declining to reveal loses no life")
    void decliningDoesNothing() {
        addCreatureReady(player1, new NightshadeSchemers());
        NightshadeSchemers topCard = new NightshadeSchemers();
        harness.setLibrary(player1, List.of(topCard));

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of(new BallyrushBanneret())); // Kithkin Soldier — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship recognizes a card sharing only one of the source's creature types")
    void sharedSingleTypePrompts() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of(new StonybrookBanneret())); // Merfolk Wizard — shares Wizard

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new NightshadeSchemers());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
