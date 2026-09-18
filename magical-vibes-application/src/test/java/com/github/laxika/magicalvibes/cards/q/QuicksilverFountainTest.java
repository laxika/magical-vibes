package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuicksilverFountain.class, Forest.class, Island.class})
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

        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A flooded land remains an Island after Quicksilver Fountain leaves")
    void floodedLandRemainsIslandAfterFountainLeaves() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new QuicksilverFountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.FLOOD, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, fountain));

        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The upkeep trigger offers only the active player's non-Island lands")
    void offersOnlyActivePlayersNonIslandLands() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent activeIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent activeForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(activeForest.getId());

        harness.handlePermanentChosen(player1, activeForest.getId());
        harness.passBothPriorities();

        assertThat(activeForest.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(activeIsland.getCounterCount(CounterType.FLOOD)).isZero();
        assertThat(opponentForest.getCounterCount(CounterType.FLOOD)).isZero();
    }

    @Test
    @DisplayName("A land with a flood counter is not offered again while it remains an Island")
    void doesNotOfferAlreadyFloodedLand() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent floodedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent freshForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        floodedForest.setCounterCount(CounterType.FLOOD, 1);

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(freshForest.getId());
    }

    @Test
    @DisplayName("At an opponent's upkeep, the opponent chooses their own non-Island land")
    void putsFloodCounterOnOpponentsNonIslandLand() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(player2Forest.getId());

        harness.handlePermanentChosen(player2, player2Forest.getId());
        harness.passBothPriorities();

        assertThat(player1Forest.getCounterCount(CounterType.FLOOD)).isZero();
        assertThat(player2Forest.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
    }

    @Test
    @DisplayName("The upkeep trigger is not put on the stack without a legal land target")
    void doesNotTriggerWithoutLegalLandTarget() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
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

    @Test
    @DisplayName("Flood counters remain when a non-Island land appears before cleanup resolves")
    void keepsFloodCountersWhenConditionFailsBeforeResolution() {
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent floodedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        floodedForest.setCounterCount(CounterType.FLOOD, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        assertThat(floodedForest.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
