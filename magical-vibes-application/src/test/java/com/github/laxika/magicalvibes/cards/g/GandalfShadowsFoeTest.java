package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GandalfShadowsFoe.class, Forest.class})
class GandalfShadowsFoeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB flickers up to three lands tapped and landfall draws and adds counters")
    void flickersLandsAndTriggersLandfall() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent thirdForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GandalfShadowsFoe()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0, List.of(firstForest.getId(), secondForest.getId(), thirdForest.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent gandalf = findPermanent(player1, "Gandalf, Shadow's Foe");
        assertThat(gandalf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(findPermanents(player1, "Forest")).hasSize(3)
                .allSatisfy(forest -> assertThat(forest.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Cannot target an opponent's land")
    void cannotTargetOpponentLand() {
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GandalfShadowsFoe()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentForest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseNoLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GandalfShadowsFoe()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Forest").getId()).isEqualTo(forest.getId());
        assertThat(forest.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Gandalf, Shadow's Foe")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void landfallOnlyTriggersForControllersLands() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1, new GandalfShadowsFoe());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player2, new Forest());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gandalf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gandalf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void landfallStillDrawsAfterGandalfLeaves() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1, new GandalfShadowsFoe());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, gandalf));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gandalf, Shadow's Foe");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void returnsBorrowedLandToOwnerWithoutTriggeringControllersLandfall() {
        Forest borrowedForest = new Forest();
        borrowedForest.setOwnerId(player2.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, borrowedForest);
        harness.setHand(player1, List.of(new GandalfShadowsFoe()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0, List.of(forest.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").getId()).isNotEqualTo(forest.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Gandalf, Shadow's Foe")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillFlickersRemainingLegalLand() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GandalfShadowsFoe()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0, List.of(firstForest.getId(), secondForest.getId()));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstForest));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Forest").getId()).isNotEqualTo(secondForest.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Gandalf, Shadow's Foe")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
