package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindlockOrb;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JestersMask.class, GrizzlyBears.class, Shock.class, Swamp.class})
class JestersMaskTest extends BaseCardTest {

    private void addMaskReady() {
        harness.addToBattlefield(player1, new JestersMask());
        Permanent mask = findPermanent(player1, "Jester's Mask");
        mask.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new JestersMask()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Jester's Mask").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's hand goes to their library and the controller picks their new hand")
    void replacesOpponentHand() {
        Card oldA = new GrizzlyBears();
        Card oldB = new Shock();
        harness.setHand(player2, List.of(oldA, oldB));

        Card deckA = new Swamp();
        Card deckB = new Swamp();
        Card deckC = new GrizzlyBears();
        harness.setLibrary(player2, List.of(deckA, deckB, deckC));

        addMaskReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Two cards were put on the library, so exactly two picks are made from the five-card library.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(oldA.getId(), oldB.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Jester's Mask");
    }

    @Test
    @DisplayName("The old hand cards are searchable and can be handed straight back")
    void oldHandCardsRemainSearchable() {
        Card oldA = new GrizzlyBears();
        harness.setHand(player2, List.of(oldA));
        harness.setLibrary(player2, List.of());

        addMaskReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // The only card in the library is the card that came from hand.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getId()).isEqualTo(oldA.getId());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        addMaskReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("The replacement search cannot be declined")
    void replacementSearchIsMandatory() {
        Card oldA = new GrizzlyBears();
        harness.setHand(player2, List.of(oldA));
        harness.setLibrary(player2, List.of());

        addMaskReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThatThrownBy(() -> gs.handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(-1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(oldA.getId());
    }

    @Test
    @DisplayName("Empty hand searches for nothing but still sacrifices the mask")
    void emptyHand() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Swamp(), new Swamp()));

        addMaskReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Jester's Mask");
    }

    @Test
    @CardUsed(MindlockOrb.class)
    @DisplayName("A prevented search leaves the moved hand in the target's library")
    void preventedSearchLeavesHandInLibrary() {
        Card oldA = new GrizzlyBears();
        Card oldB = new Shock();
        Card deckA = new Swamp();
        harness.setHand(player2, List.of(oldA, oldB));
        harness.setLibrary(player2, List.of(deckA));

        addMaskReady();
        harness.addToBattlefield(player1, new MindlockOrb());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(deckA, oldA, oldB);
        harness.assertInGraveyard(player1, "Jester's Mask");
    }
}
