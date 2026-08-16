package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KaylasReconstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to X eligible cards onto the battlefield and bottoms the rest randomly")
    void putsUpToXEligibleCardsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card mindStone = new MindStone();
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card forest = new Forest();
        Card angel = new SerraAngel();
        setUpGame(List.of(bears, mindStone, elves, shock, forest, angel));

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), mindStone.getId(), elves.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), mindStone.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Shock", "Forest", "Serra Angel");
    }

    @Test
    @DisplayName("With X equal to zero, puts all looked-at cards on the bottom")
    void withZeroXDoesNotPutCardsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card mindStone = new MindStone();
        Card shock = new Shock();
        setUpGame(List.of(bears, mindStone, shock));

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Mind Stone", "Shock");
    }

    private void setUpGame(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new KaylasReconstruction()));
    }
}
