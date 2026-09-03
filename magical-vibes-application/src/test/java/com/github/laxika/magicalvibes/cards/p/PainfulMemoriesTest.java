package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PainfulMemories.class, GrizzlyBears.class})
class PainfulMemoriesTest extends BaseCardTest {

    private PainfulMemories castPainfulMemories() {
        PainfulMemories spell = new PainfulMemories();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        return spell;
    }

    @Test
    @DisplayName("Choosing a card puts it on top of the opponent's library")
    void choosingCardPutsItOnTop() {
        Card card1 = new GrizzlyBears();
        Card card2 = new PainfulMemories();
        harness.setHand(player2, List.of(card1, card2));

        castPainfulMemories();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);
        assertThat(choice.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck.get(0)).isSameAs(card1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card2);
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());

        castPainfulMemories();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new PainfulMemories()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Only the caster can choose the card")
    void onlyCasterCanChooseCard() {
        Card card = new GrizzlyBears();
        harness.setHand(player2, List.of(card));

        castPainfulMemories();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("Does not expose the looked-at hand in the public game log")
    void doesNotExposeLookedAtHandInPublicGameLog() {
        Card card = new GrizzlyBears();
        harness.setHand(player2, List.of(card));

        castPainfulMemories();

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()).toList())
                .noneMatch(message -> message.contains(card.getName()));
    }

    @Test
    @DisplayName("Goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        PainfulMemories spell = castPainfulMemories();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }
}
