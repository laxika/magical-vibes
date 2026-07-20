package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvenMindcensorTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's tutor search only sees the top four cards of their library")
    void opponentSearchLimitedToTopFour() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        setupTutor(player1);
        setSixCardLibrary(player1);

        harness.passBothPriorities(); // resolve Diabolic Tutor -> search prompt

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        // Only the top four cards (Plains, Swamp, Forest, Island) are searchable; the two
        // Grizzly Bears deeper in the library are hidden.
        assertThat(search.params().cards()).hasSize(4);
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Swamp", "Forest", "Island");
    }

    @Test
    @DisplayName("A card found among the top four is put into hand")
    void choosingFromTopFourPutsCardInHand() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        setupTutor(player1);
        setSixCardLibrary(player1);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player's own Aven Mindcensor does not limit their own search")
    void ownAvenMindcensorDoesNotLimitOwnSearch() {
        harness.addToBattlefield(player1, new AvenMindcensor());
        setupTutor(player1);
        setSixCardLibrary(player1);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Aven Mindcensor only affects opponents, so player1 still searches their whole library.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(6);
    }

    @Test
    @DisplayName("A restricted search that finds nothing in the top four finds nothing at all")
    void noMatchingCardInTopFourFindsNothing() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);

        // Only basic land is a Forest sitting fifth; the top four are all Grizzly Bears.
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("top 4 cards"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    private void setupTutor(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new DiabolicTutor()));
        harness.addMana(player, ManaColor.BLACK, 4);
        harness.castSorcery(player, 0, 0);
    }

    private void setSixCardLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new Forest(), new Island(),
                new GrizzlyBears(), new GrizzlyBears()));
    }
}
