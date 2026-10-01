package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KithkinZephyrnaut;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SqueakingPieGrubfellows.class, KithkinZephyrnaut.class})
class SqueakingPieGrubfellowsTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to look when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows())); // Goblin Shaman — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Looking and revealing the shared-type card are separate choices")
    void lookingAndRevealingAreSeparateChoices() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, new ArrayList<>(List.of(new KithkinZephyrnaut())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Revealing the shared-type card makes each opponent discard a card")
    void revealMakesOpponentDiscard() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, new ArrayList<>(List.of(new KithkinZephyrnaut(), new KithkinZephyrnaut())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining to reveal makes no opponent discard")
    void decliningDoesNothing() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new SqueakingPieGrubfellows()));
        harness.setHand(player2, new ArrayList<>(List.of(new KithkinZephyrnaut())));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Looking at a nonmatching top card offers no reveal choice")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new SqueakingPieGrubfellows());
        harness.setLibrary(player1, List.of(new KithkinZephyrnaut())); // Kithkin Soldier — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
