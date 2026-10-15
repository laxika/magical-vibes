package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirstingRoots.class, Forest.class, CopperLonglegs.class})
class ThirstingRootsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and puts it into its owner's hand")
    void searchesForBasicLand() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new CopperLonglegs()));
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gameData.playerHands.get(player1.getId())).contains(forest);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Proliferates onto a permanent with a counter")
    void proliferates() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    void mayFailToFindEvenWhenBasicLandIsAvailable() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchWithNoBasicLandFinishesWithoutTakingAnotherCard() {
        CopperLonglegs spider = new CopperLonglegs();
        harness.setLibrary(player1, List.of(spider));
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spider);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void proliferatesEachExistingKindOnSelectedPermanentsAndPlayers() {
        Permanent selected = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        selected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        selected.setCounterCount(CounterType.CHARGE, 3);
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId(), player2.getId()));

        assertThat(selected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(selected.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseNothingToProliferate() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void proliferateWithoutCountersFinishesWithoutSearchingLibrary() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player1, new CopperLonglegs());
        harness.setHand(player1, List.of(new ThirstingRoots()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
