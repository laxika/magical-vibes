package com.github.laxika.magicalvibes.cards.f;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.a.AvenSquire;
import com.github.laxika.magicalvibes.cards.m.ManaforceMace;
import com.github.laxika.magicalvibes.cards.r.RuptureSpire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieMechanist.class, AvenSquire.class, RuptureSpire.class, ManaforceMace.class})
class FaerieMechanistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only the artifact card among the top three")
    void etbOffersOnlyArtifact() {
        setupTopCards(List.of(new FaerieMechanist(), new AvenSquire(), new RuptureSpire()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered.getFirst().getName()).isEqualTo("Faerie Mechanist");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing the artifact puts it into hand, rest go on bottom")
    void choosingArtifactPutsIntoHand() {
        setupTopCards(List.of(new FaerieMechanist(), new AvenSquire(), new RuptureSpire()));
        castAndResolveEtb();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Faerie Mechanist");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Declining puts nothing in hand and orders all three on bottom")
    void decliningReordersAllToBottom() {
        setupTopCards(List.of(new FaerieMechanist(), new AvenSquire(), new RuptureSpire()));
        castAndResolveEtb();

        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Faerie Mechanist");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("With no artifact among the top three, they are put on bottom directly")
    void noArtifactReordersDirectly() {
        setupTopCards(List.of(new AvenSquire(), new RuptureSpire(), new AvenSquire()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    void choosesOnlyOneArtifactAndOrdersRestBelowUntouchedLibrary() {
        Card creatureArtifact = new FaerieMechanist();
        Card equipment = new ManaforceMace();
        Card nonartifact = new AvenSquire();
        Card untouched = new RuptureSpire();
        setupTopCards(List.of(creatureArtifact, equipment, nonartifact, untouched));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(creatureArtifact, equipment);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(creatureArtifact, nonartifact);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, nonartifact, creatureArtifact);
    }

    @Test
    void loneArtifactCanBeDeclined() {
        Card artifact = new ManaforceMace();
        setupTopCards(List.of(artifact));
        castAndResolveEtb();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void twoCardLibraryAllowsArtifactSelection() {
        Card artifact = new ManaforceMace();
        Card other = new AvenSquire();
        setupTopCards(List.of(other, artifact));
        castAndResolveEtb();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new FaerieMechanist()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve the creature spell and put its enter trigger on the stack.
        harness.passBothPriorities(); // Resolve the enter trigger.
    }
}
