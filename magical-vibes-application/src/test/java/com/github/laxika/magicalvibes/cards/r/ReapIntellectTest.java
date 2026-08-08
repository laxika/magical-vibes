package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReapIntellectTest extends BaseCardTest {

    private void castReapIntellect(int xValue) {
        harness.setHand(player1, List.of(new ReapIntellect()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, xValue, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Only nonland cards in the revealed hand can be chosen")
    void onlyNonlandCardsChoosable() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new LightningBolt())));

        castReapIntellect(1);

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 2);
    }

    @Test
    @DisplayName("A chosen card is exiled along with every copy in hand, graveyard, and library")
    void exilesChosenCardAndAllCopies() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        castReapIntellect(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Lightning Bolt");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Serra Angel");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsOnly("Grizzly Bears")
                .hasSize(4);
    }

    @Test
    @DisplayName("X=2 exiles two different names and all of their copies")
    void exilesTwoChosenNames() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new LightningBolt(), new Forest())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new LightningBolt(), new SerraAngel())));

        castReapIntellect(2);
        harness.handleCardChosen(player1, 0);
        // The nonland restriction still applies to the second pick — only Lightning Bolt is choosable.
        PendingInteraction.RevealedHandChoice second =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(second.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Lightning Bolt", "Lightning Bolt");
    }

    @Test
    @DisplayName("Choosing fewer than X cards still exiles the cards already chosen")
    void decliningAfterFirstPickStillExiles() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new LightningBolt())));

        castReapIntellect(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Lightning Bolt");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A hand of only lands leaves everything untouched")
    void handOfOnlyLandsDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest())));

        castReapIntellect(2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("X=0 exiles nothing and prompts no choice")
    void xZeroExilesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new LightningBolt())));

        castReapIntellect(0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
