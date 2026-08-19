package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhirOfInventionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only artifacts with mana value <= X")
    void presentsOnlyArtifactsWithinManaValueBound() {
        castWhir(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Ornithopter", "Bonesplitter", "Myr Retriever");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Choosing an artifact puts it onto the battlefield")
    void chosenArtifactEntersBattlefield() {
        castWhir(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosen));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosen));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        harness.assertInGraveyard(player1, "Whir of Invention");
    }

    @Test
    @DisplayName("Improvise lets an artifact pay the generic part of Whir of Invention")
    void improvisePaysGenericMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        harness.setHand(player1, List.of(new WhirOfInvention()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 1, null, null,
                List.of(), List.of(artifact.getId()), false, null);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X=0 finds zero-mana-value artifacts only")
    void xZeroFindsZeroManaValueArtifactsOnly() {
        castWhir(0);
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = harness.getGameData()
                .interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Ornithopter");
    }

    private void castWhir(int xValue) {
        harness.setHand(player1, List.of(new WhirOfInvention()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 3);
        harness.castInstant(player1, 0, xValue, null);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Ornithopter(), new Bonesplitter(), new MyrRetriever(),
                new DarksteelIngot(), new GrizzlyBears()));
    }
}
