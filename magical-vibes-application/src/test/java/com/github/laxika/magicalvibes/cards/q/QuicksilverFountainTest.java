package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuicksilverFountainTest extends BaseCardTest {

    @Test
    @DisplayName("At each upkeep, the active player puts a flood counter on a non-Island land they control")
    void putsFloodCounterOnActivePlayersNonIslandLand() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(opponentForest.getCounterCount(CounterType.FLOOD)).isZero();
    }

    @Test
    @DisplayName("A flooded land is an Island and produces blue mana")
    void floodedLandBecomesIsland() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.FLOOD, 1);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);

        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.getOverriddenLandManaColor(gd, forest)).isEqualTo(ManaColor.BLUE);
    }

    @Test
    @DisplayName("At end step, all flood counters are removed when every land is an Island")
    void removesFloodCountersWhenAllLandsAreIslands() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        forest.setCounterCount(CounterType.FLOOD, 1);
        opponentIsland.setCounterCount(CounterType.FLOOD, 2);

        advanceToEndStep(player1);

        assertThat(forest.getCounterCount(CounterType.FLOOD)).isZero();
        assertThat(opponentIsland.getCounterCount(CounterType.FLOOD)).isZero();
    }

    @Test
    @DisplayName("Flood counters remain when a non-Island land is present")
    void keepsFloodCountersWhenNonIslandLandRemains() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent floodedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent nonIslandLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        floodedForest.setCounterCount(CounterType.FLOOD, 1);

        advanceToEndStep(player1);

        assertThat(floodedForest.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(nonIslandLand.getCounterCount(CounterType.FLOOD)).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
