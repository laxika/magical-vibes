package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PanglacialWurmTest extends BaseCardTest {

    @Test
    void canCastFromLibraryBeforeFindingSearchCard() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 9);

        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int wurmIndex = indexOfCard(search.params().cards(), wurm);
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(wurmIndex));

        assertThat(gameData.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gameData.stack).anyMatch(entry -> entry.getCard().getId().equals(wurm.getId()));

        search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int forestIndex = indexOfCard(search.params().cards(), forest);
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(forestIndex));
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wurm.getId()));
    }

    @Test
    void cannotCastAfterFindingSearchCard() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 2);

        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int forestIndex = indexOfCard(search.params().cards(), forest);
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(forestIndex));

        assertThat(gameData.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
        assertThat(gameData.stack).noneMatch(entry -> entry.getCard().getId().equals(wurm.getId()));
    }

    @Test
    void cannotCastWithoutMana() {
        PanglacialWurm wurm = new PanglacialWurm();
        Forest forest = new Forest();
        castRampantGrowth(wurm, forest, 2);

        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int wurmIndex = indexOfCard(search.params().cards(), wurm);

        assertThatThrownBy(() -> harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(wurmIndex)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        assertThat(gameData.playerDecks.get(player1.getId())).anyMatch(card -> card.getId().equals(wurm.getId()));
    }

    private void castRampantGrowth(PanglacialWurm wurm, Forest forest, int mana) {
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.setLibrary(player1, List.of(wurm, forest));
        harness.addMana(player1, ManaColor.GREEN, mana);
        harness.castSorcery(player1, 0, 0);
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
