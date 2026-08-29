package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThirstingRootsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and puts it into its owner's hand")
    void searchesForBasicLand() {
        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gameData.playerHands.get(player1.getId())).contains(forest);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Proliferates onto a permanent with a counter")
    void proliferates() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
