package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlowUnder.class, Forest.class, Mountain.class, GrizzlyBears.class})
class PlowUnderTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Puts both target lands on top of their owners' libraries")
    void putsBothTargetLandsOnTopOfLibraries() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(forestId, mountainId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 2);
        assertThat(deck.stream().limit(2).map(Card::getName))
                .containsExactlyInAnyOrder("Forest", "Mountain");
    }

    @Test
    @DisplayName("Targets each land's own owner's library")
    void tucksLandsToTheirOwnOwnersLibrary() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        UUID myForestId = harness.getPermanentId(player1, "Forest");
        UUID theirMountainId = harness.getPermanentId(player2, "Mountain");
        int p1DeckBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        int p2DeckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(myForestId, theirMountainId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Mountain");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore + 1);
    }

    @Test
    @DisplayName("Puts the remaining land on top when the other target is gone")
    void resolvesWithOneLegalTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();
        harness.castSorcery(player1, 0, List.of(forestId, mountainId));

        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(forestId));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Mountain");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forestId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same land twice")
    void cannotTargetSameLandTwice() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forestId, forestId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The lands' owner chooses their order on top of the library")
    void ownerChoosesOrderOfBothLands() {
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.addToBattlefield(player2, forest);
        harness.addToBattlefield(player2, mountain);
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        List<Card> originalDeck = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();
        harness.castAndResolveSorcery(player1, 0, List.of(forestId, mountainId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.playerId()).isEqualTo(player2.getId());
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(reorder.toBottom()).isFalse();
        assertThat(reorder.cards()).containsExactlyInAnyOrder(forest, mountain);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(
                List.of(reorder.cards().indexOf(forest), reorder.cards().indexOf(mountain))));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2)).containsExactly(forest, mountain);
        assertThat(gd.playerDecks.get(player2.getId()).subList(2, originalDeck.size() + 2))
                .containsExactlyElementsOf(originalDeck);
        harness.assertInGraveyard(player1, "Plow Under");
    }

    @Test
    @DisplayName("Does not resolve when both targets have left the battlefield")
    void doesNotResolveWithNoLegalTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        List<Card> originalDeck = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();
        harness.castSorcery(player1, 0, List.of(forestId, mountainId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(originalDeck);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Plow Under");
    }

    @Test
    @DisplayName("Cannot cast with only one target land")
    void requiresTwoTargetLands() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new PlowUnder()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
