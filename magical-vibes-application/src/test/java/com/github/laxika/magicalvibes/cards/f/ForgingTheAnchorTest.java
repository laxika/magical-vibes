package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForgingTheAnchorTest extends BaseCardTest {

    @Test
    @DisplayName("Offers every artifact card among the top five")
    void offersEveryArtifactAmongTopFive() {
        ChromaticStar star = new ChromaticStar();
        Ornithopter ornithopter = new Ornithopter();
        setupTopFive(List.of(star, new GrizzlyBears(), ornithopter, new Shock(), new Island()));
        cast();

        PendingInteraction.LibraryRevealChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(star.getId(), ornithopter.getId());
    }

    @Test
    @DisplayName("Puts multiple chosen artifacts into hand and the rest on the bottom")
    void choosesMultipleArtifactsAndBottomsRest() {
        ChromaticStar star = new ChromaticStar();
        Ornithopter ornithopter = new Ornithopter();
        setupTopFive(List.of(star, new GrizzlyBears(), ornithopter, new Shock(), new Island()));
        cast();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(star.getId(), ornithopter.getId()));

        harness.assertInHand(player1, "Chromatic Star");
        harness.assertInHand(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Shock", "Island", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose no artifacts and bottom all five cards")
    void mayChooseNoArtifacts() {
        setupTopFive(List.of(new ChromaticStar(), new GrizzlyBears(), new Ornithopter(), new Shock(), new Island()));
        cast();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .doesNotContain("Chromatic Star", "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Chromatic Star", "Grizzly Bears", "Ornithopter", "Shock", "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new ForgingTheAnchor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopFive(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
