package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deception.class, Forest.class, Island.class, Mountain.class})
class DeceptionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting opponent")
    void castingPutsOnStack() {
        castDeception();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Deception()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Opponent discards two cards of their choice")
    void opponentDiscardsTwoCards() {
        harness.setHand(player2, List.of(new Forest(), new Island(), new Mountain()));
        castAndResolveDeception();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst()).isInstanceOf(Mountain.class);
    }

    @Test
    @DisplayName("Opponent with one card discards that card without a second prompt")
    void oneCardHandDiscardsWhatIsAvailable() {
        harness.setHand(player2, List.of(new Forest()));
        castAndResolveDeception();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Opponent with empty hand results in no discard prompt")
    void emptyHandNoPrompt() {
        harness.setHand(player2, List.of());
        castAndResolveDeception();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("no cards to discard")).isTrue();
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of(new Forest(), new Island()));
        castAndResolveDeception();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deception");
    }

    @Test
    @DisplayName("Opponent can keep the first card and the caster's remaining hand is unaffected")
    void opponentChoosesNonFirstCardsAndCasterKeepsHand() {
        Forest retained = new Forest();
        Island discardedIsland = new Island();
        Mountain discardedMountain = new Mountain();
        Forest casterCard = new Forest();
        harness.setHand(player1, List.of(new Deception(), casterCard));
        harness.setHand(player2, List.of(retained, discardedIsland, discardedMountain));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(discardedIsland, discardedMountain);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deception");
    }

    private void castAndResolveDeception() {
        harness.setHand(player1, List.of(new Deception()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private void castDeception() {
        harness.setHand(player1, List.of(new Deception()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
    }
}
