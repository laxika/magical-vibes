package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NaturalBalanceTest extends BaseCardTest {

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private void setForestLibrary(Player player, int count) {
        List<com.github.laxika.magicalvibes.model.Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player, library);
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    private List<UUID> landIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private void castNaturalBalance() {
        harness.setHand(player1, List.of(new NaturalBalance()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A player with six or more lands sacrifices down to five, choosing which to lose")
    void sacrificesDownToFiveLands() {
        addForests(player1, 7);
        addForests(player2, 5);

        castNaturalBalance();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(landCount(player2)).isEqualTo(5);
    }

    @Test
    @DisplayName("A player with four or fewer lands may search for five minus their land count basic lands")
    void searchesUpToFiveMinusLandCount() {
        addForests(player1, 2);
        addForests(player2, 5);
        setForestLibrary(player1, 6);

        castNaturalBalance();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(3);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The search is optional — declining leaves the searcher's lands untouched")
    void searchMayBeDeclined() {
        addForests(player1, 3);
        addForests(player2, 5);
        setForestLibrary(player1, 4);

        castNaturalBalance();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Searches resolve before the forced sacrifices, and both halves happen")
    void searchThenSacrificeBothResolve() {
        addForests(player1, 7);
        addForests(player2, 1);
        setForestLibrary(player2, 6);

        castNaturalBalance();

        // player2 controls one land, so they search first for up to four basic lands.
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(4);

        for (int i = 0; i < 4; i++) {
            harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));
        }

        // Only then does player1 choose which two of their seven lands to sacrifice.
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(landCount(player2)).isEqualTo(5);
    }

    @Test
    @DisplayName("Players with exactly five lands neither sacrifice nor search")
    void exactlyFiveLandsIsUntouched() {
        addForests(player1, 5);
        addForests(player2, 5);
        setForestLibrary(player1, 4);

        castNaturalBalance();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(landCount(player2)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only lands count — nonland permanents are neither counted nor sacrificed")
    void onlyLandsAreCountedAndSacrificed() {
        addForests(player1, 6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        addForests(player2, 5);

        castNaturalBalance();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).hasSize(6);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }
}
