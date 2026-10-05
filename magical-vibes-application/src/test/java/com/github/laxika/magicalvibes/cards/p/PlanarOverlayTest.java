package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IsolatedChapel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.cards.n.NefariousImp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Planar Overlay")
@CardUsed({PlanarOverlay.class, Forest.class, Island.class, IsolatedChapel.class,
        Mountain.class, TropicalIsland.class, Plains.class, Swamp.class, NefariousImp.class})
class PlanarOverlayTest extends BaseCardTest {

    @Test
    @DisplayName("Each player returns one land of each basic land type they control")
    void eachPlayerReturnsOneLandOfEachBasicLandType() {
        Permanent player1ReturnedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1RemainingForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent player1NonbasicLand = harness.addToBattlefieldAndReturn(player1, new IsolatedChapel());

        Permanent player2ReturnedMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent player2RemainingMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2NonbasicLand = harness.addToBattlefieldAndReturn(player2, new IsolatedChapel());

        harness.castFromHand(player1, new PlanarOverlay(), "{2}{U}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(
                player1ReturnedForest.getId(), player1RemainingForest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1ReturnedForest.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(player1ReturnedForest, player1RemainingForest, player1Island, player1NonbasicLand);

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(
                player2ReturnedMountain.getId(), player2RemainingMountain.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2ReturnedMountain.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(player1RemainingForest.getId(), player1NonbasicLand.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(player2RemainingMountain.getId(), player2NonbasicLand.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(player1ReturnedForest.getCard(), player1Island.getCard());
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(player2ReturnedMountain.getCard(), player2Forest.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(player1ReturnedForest, player1Island);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(player2ReturnedMountain, player2Forest);
    }

    @Test
    @DisplayName("A land with multiple basic land types can satisfy multiple choices")
    void multiTypedLandCanBeChosenForEachBasicLandType() {
        Permanent chosenTropicalIsland = harness.addToBattlefieldAndReturn(player1, new TropicalIsland());
        Permanent remainingTropicalIsland = harness.addToBattlefieldAndReturn(player1, new TropicalIsland());

        harness.castFromHand(player1, new PlanarOverlay(), "{2}{U}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice islandChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(islandChoice).isNotNull();
        assertThat(islandChoice.validIds()).containsExactly(
                chosenTropicalIsland.getId(), remainingTropicalIsland.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenTropicalIsland.getId()));

        PendingInteraction.MultiPermanentChoice forestChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(forestChoice).isNotNull();
        assertThat(forestChoice.validIds()).containsExactly(
                chosenTropicalIsland.getId(), remainingTropicalIsland.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenTropicalIsland.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(remainingTropicalIsland.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(chosenTropicalIsland.getCard());
    }

    @Test
    @DisplayName("Returns all five basic land types without choices when each is unique")
    void returnsAllFiveUniqueBasicLandTypes() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new PlanarOverlay(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(
                plains.getCard(), island.getCard(), swamp.getCard(), mountain.getCard(), forest.getCard());
        harness.assertInGraveyard(player1, "Planar Overlay");
    }

    @Test
    @DisplayName("Resolves without returning lands that have no basic land types")
    void leavesUntypedLandsOnBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IsolatedChapel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new IsolatedChapel());

        harness.castFromHand(player1, new PlanarOverlay(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Planar Overlay");
    }

    @Test
    @DisplayName("Different dual lands may be chosen for their different basic land types")
    void canChooseDifferentDualLandsForEachType() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TropicalIsland());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TropicalIsland());

        harness.castFromHand(player1, new PlanarOverlay(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("Returns chosen lands simultaneously for one-or-more leave triggers")
    void returningMultipleLandsProducesOneLeaveTrigger() {
        harness.addToBattlefield(player1, new NefariousImp());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.castFromHand(player1, new PlanarOverlay(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
