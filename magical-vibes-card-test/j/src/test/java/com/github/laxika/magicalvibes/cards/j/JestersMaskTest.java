package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.m.MindlockOrb;
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

@CardUsed({JestersMask.class, BalduvianBears.class, Incinerate.class, Swamp.class, MindlockOrb.class})
class JestersMaskTest extends BaseCardTest {

    private void addMaskReady() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new JestersMask());
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
        Card oldA = new BalduvianBears();
        Card oldB = new Incinerate();
        harness.setHand(player2, List.of(oldA, oldB));

        Card deckA = new Swamp();
        Card deckB = new Swamp();
        Card deckC = new BalduvianBears();
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
        Card oldA = new BalduvianBears();
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
        Card oldA = new BalduvianBears();
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
    @DisplayName("A prevented search leaves the moved hand in the target's library")
    void preventedSearchLeavesHandInLibrary() {
        Card oldA = new BalduvianBears();
        Card oldB = new Incinerate();
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

    @Test
    @DisplayName("Controller can choose cards from the original library for the opponent")
    void choosesDifferentReplacementCards() {
        Card oldA = new BalduvianBears();
        Card oldB = new Incinerate();
        Card replacementA = new Swamp();
        Card replacementB = new Swamp();
        harness.setHand(player2, List.of(oldA, oldB));
        harness.setLibrary(player2, List.of(replacementA, replacementB));
        harness.setHand(player1, List.of());

        addMaskReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.handleInteractionAnswer(
                gd, player2, new InteractionAnswer.LibraryCardChosen(2)))
                .isInstanceOf(IllegalStateException.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(2));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(2));

        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(replacementA, replacementB);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(oldA, oldB);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Sacrifice is paid at activation before the opponent's hand changes")
    void sacrificesAsActivationCost() {
        Card oldCard = new BalduvianBears();
        harness.setHand(player2, List.of(oldCard));
        harness.setLibrary(player2, List.of());
        addMaskReady();

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Jester's Mask");
        harness.assertInGraveyard(player1, "Jester's Mask");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(oldCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(oldCard);
    }

    @Test
    @DisplayName("Cannot activate while tapped after entering the battlefield")
    void cannotActivateWhileTapped() {
        Permanent mask = harness.enterBattlefieldAndReturn(player1, new JestersMask());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(mask.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Jester's Mask");
        harness.assertNotInGraveyard(player1, "Jester's Mask");
    }
}
