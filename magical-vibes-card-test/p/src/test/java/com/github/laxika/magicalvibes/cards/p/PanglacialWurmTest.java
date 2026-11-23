package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PanglacialWurm.class, Forest.class, RampantGrowth.class, DiabolicTutor.class, Silence.class, RuleOfLaw.class})
class PanglacialWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast from the library while searching, before the searched card is found")
    void canCastFromLibraryBeforeFindingSearchCard() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 9);

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int wurmIndex = indexOfCard(search.params().cards(), wurm);
        harness.handleCardChosen(player1, wurmIndex);

        assertThat(gameData.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gameData.stack).anyMatch(entry -> entry.getCard().getId().equals(wurm.getId()));

        search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int forestIndex = indexOfCard(search.params().cards(), forest);
        harness.handleCardChosen(player1, forestIndex);
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Can't be cast once the search has found its card")
    void cannotCastAfterFindingSearchCard() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 2);

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int forestIndex = indexOfCard(search.params().cards(), forest);
        harness.handleCardChosen(player1, forestIndex);

        assertThat(gameData.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gameData.stack).noneMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Can't be cast while searching without the mana to pay for it")
    void cannotCastWithoutMana() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 2);

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int wurmIndex = indexOfCard(search.params().cards(), wurm);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, wurmIndex))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gameData.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Can be cast during a search that finds nothing")
    void canCastWhileSearchingWithNoMatchingLand() {
        PanglacialWurm wurm = new PanglacialWurm();
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.setLibrary(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), wurm));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Panglacial Wurm");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tutor can find it without casting it")
    void canFindWurmWithTutorWithoutCastingIt() {
        PanglacialWurm wurm = new PanglacialWurm();
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.setLibrary(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), wurm));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Several can be cast during one search")
    void canCastMultipleWurmsDuringOneSearch() {
        PanglacialWurm first = new PanglacialWurm();
        PanglacialWurm second = new PanglacialWurm();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.setLibrary(player1, List.of(first, second, forest));
        harness.addMana(player1, ManaColor.GREEN, 16);
        harness.castAndResolveSorcery(player1, 0, 0);

        for (PanglacialWurm wurm : List.of(first, second)) {
            PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            harness.handleCardChosen(player1, indexOfCard(search.params().cards(), wurm));
            assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(permanent -> permanent.getCard().getId().equals(wurm.getId()));
        }

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), forest));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
    }

    @Test
    @DisplayName("The search can end without a land after casting it")
    void canFinishSearchWithoutFindingLandAfterCastingWurm() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 9);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), wurm));
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Panglacial Wurm");
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(forest.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
    }

    @Test
    @DisplayName("Can't be cast after the land is found, even with enough mana")
    void cannotCastAfterFindingLandEvenWithEnoughMana() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 9);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), forest));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Can't be cast without both green mana")
    void cannotCastWithoutBothRequiredGreenMana() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.setLibrary(player1, List.of(wurm, forest));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int wurmIndex = indexOfCard(search.params().cards(), wurm);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, wurmIndex))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Can't be cast during the search after an opponent resolves Silence")
    void cannotCastDuringSearchAfterOpponentResolvesSilence() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.setHand(player2, List.of(new Silence()));
        harness.setLibrary(player1, List.of(wurm, forest));
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        gs.passPriority(gd, player1);
        harness.castAndResolveInstant(player2, 0);
        assertThat(silencedPlayers()).contains(player1.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int wurmIndex = indexOfCard(search.params().cards(), wurm);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, wurmIndex))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
    }

    @Test
    @DisplayName("Can't be cast during the search when a spell limit has been reached")
    void cannotCastDuringSearchAtSpellLimit() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 9);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int wurmIndex = indexOfCard(search.params().cards(), wurm);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, wurmIndex))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
    }

    private void castRampantGrowth(PanglacialWurm wurm, Forest forest, int mana) {
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.setLibrary(player1, List.of(wurm, forest));
        harness.addMana(player1, ManaColor.GREEN, mana);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private int indexOfCard(List<Card> cards, Card wanted) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(wanted.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card was not offered in the library search");
    }
}
