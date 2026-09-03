package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.s.SigardaHostOfHerons;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturalBalance.class, Forest.class, FeralShadow.class, SigardaHostOfHerons.class,
        ObNixilisUnshackled.class, AvenMindcensor.class})
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
        harness.castFromHand(player1, new NaturalBalance(), "{2}{G}{G}");
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
    @DisplayName("All players choose excess lands before those lands are sacrificed")
    void multiplePlayersChooseBeforeSacrifices() {
        addForests(player1, 6);
        addForests(player2, 7);

        castNaturalBalance();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(landCount(player1)).isEqualTo(6);
        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 2));

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
    @DisplayName("Declining the optional search does not trigger an opponent's search ability")
    void decliningSearchDoesNotTriggerSearchAbility() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new FeralShadow());
        addForests(player2, 5);
        setForestLibrary(player1, 1);

        castNaturalBalance();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Feral Shadow")).isEqualTo(1);
    }

    @Test
    @DisplayName("Searching without a basic land still triggers an opponent's search ability")
    void searchWithoutBasicLandTriggersSearchAbility() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new FeralShadow());
        addForests(player2, 5);
        harness.setLibrary(player1, List.of(new FeralShadow()));

        castNaturalBalance();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(countPermanents(player1, "Feral Shadow")).isZero();
    }

    @Test
    @DisplayName("Forced sacrifices resolve before searches, and both halves happen")
    void sacrificeThenSearchBothResolve() {
        addForests(player1, 7);
        addForests(player2, 1);
        setForestLibrary(player2, 6);

        castNaturalBalance();

        // player1 controls seven lands, so they choose two lands to sacrifice first.
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        // Only then does player2 search for up to four basic lands.
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(4);

        for (int i = 0; i < 4; i++) {
            harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(landCount(player2)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's sacrifice protection prevents Natural Balance from forcing a sacrifice")
    void opponentSacrificeProtectionIsRespected() {
        addForests(player1, 5);
        addForests(player2, 6);
        harness.addToBattlefield(player2, new SigardaHostOfHerons());

        castNaturalBalance();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player2)).isEqualTo(6);
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
    @DisplayName("A restricted search with no eligible card in the top four still completes")
    void restrictedSearchWithNoTopMatchCompletes() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        addForests(player2, 5);
        harness.setLibrary(player1, List.of(
                new FeralShadow(), new FeralShadow(), new FeralShadow(), new FeralShadow(), new Forest()));

        castNaturalBalance();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(landCount(player1)).isZero();
    }

    @Test
    @DisplayName("Only lands count — nonland permanents are neither counted nor sacrificed")
    void onlyLandsAreCountedAndSacrificed() {
        addForests(player1, 6);
        harness.addToBattlefield(player1, new FeralShadow());
        addForests(player2, 5);

        castNaturalBalance();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).hasSize(6);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(landCount(player1)).isEqualTo(5);
        assertThat(countPermanents(player1, "Feral Shadow")).isEqualTo(1);
    }
}
