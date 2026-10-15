package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbandonAttachments.class, Forest.class})
class AbandonAttachmentsTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card draws two cards")
    void discardingCardDrawsTwoCards() {
        Forest discarded = new Forest();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.setHand(player1, List.of(new AbandonAttachments(), discarded));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnOne, drawnTwo);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining does not discard or draw")
    void decliningDoesNothing() {
        Forest discarded = new Forest();
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(new AbandonAttachments(), discarded));
        harness.setLibrary(player1, List.of(libraryCard));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with no card in hand does not draw")
    void acceptingWithNoCardToDiscardDoesNothing() {
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of(new AbandonAttachments()));
        harness.setLibrary(player1, List.of(libraryCard));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Red mana pays the hybrid cost and the chosen card is discarded before drawing")
    void redManaAndChoosingSecondCard() {
        Forest retained = new Forest();
        AbandonAttachments discarded = new AbandonAttachments();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.setHand(player1, List.of(new AbandonAttachments(), retained, discarded));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).validIndices())
                .containsExactly(0, 1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(retained, drawnOne, drawnTwo);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).doesNotContain(retained);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
