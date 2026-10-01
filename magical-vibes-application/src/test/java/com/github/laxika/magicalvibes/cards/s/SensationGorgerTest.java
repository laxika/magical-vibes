package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KithkinZephyrnaut;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SensationGorger.class, KithkinZephyrnaut.class})
class SensationGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger()); // Goblin Shaman — shares a type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing makes each player discard their hand and draw four cards")
    void revealDiscardsHandsAndDrawsFour() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());
        harness.setHand(player1, List.of(new SensationGorger(), new SensationGorger()));
        harness.setHand(player2, List.of(new SensationGorger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining to reveal leaves hands untouched")
    void decliningDoesNothing() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());
        harness.setHand(player1, List.of(new SensationGorger(), new SensationGorger()));
        harness.setHand(player2, List.of(new SensationGorger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new KithkinZephyrnaut()); // Kithkin Soldier — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An empty library produces no reveal prompt")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new SensationGorger());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kinship uses the source's last known creature types if it leaves before resolution")
    void usesLastKnownSourceAfterLeavingBattlefield() {
        Permanent source = addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Kinship's accepted reveal still resolves after the source leaves")
    void acceptedRevealResolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new SensationGorger());
        setLibraryTop(new SensationGorger());
        harness.setHand(player1, List.of(new SensationGorger(), new SensationGorger()));
        harness.setHand(player2, List.of(new SensationGorger()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private void setLibraryTop(Card card) {
        harness.setLibrary(player1, List.of(
                card,
                new SensationGorger(),
                new SensationGorger(),
                new SensationGorger(),
                new SensationGorger()));
    }
}
