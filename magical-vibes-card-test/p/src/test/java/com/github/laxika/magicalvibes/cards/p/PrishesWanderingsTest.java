package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CapitalCity;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gaelicat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrishesWanderings.class, Forest.class, Gaelicat.class, CapitalCity.class})
class PrishesWanderingsTest extends BaseCardTest {

    @Test
    void fetchesBasicLandAndPutsCounterOnTargetAfterSeparateTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Gaelicat()));

        harness.castFromHand(player1, new PrishesWanderings(), "{2}{G}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void fetchesNonbasicTownWithoutControllingACreature() {
        Card town = new CapitalCity();
        harness.setLibrary(player1, List.of(town));

        harness.castFromHand(player1, new PrishesWanderings(), "{2}{G}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Capital City").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseOpponentCreatureForReflexiveTrigger() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        harness.addToBattlefield(player1, new Gaelicat());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Gaelicat());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new PrishesWanderings(), "{2}{G}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, own.getId());
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void failingToFindStillCreatesCounterTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        harness.addToBattlefield(player1, new Gaelicat());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromHand(player1, new PrishesWanderings(), "{2}{G}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void searchingEmptyLibraryStillCreatesCounterTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Gaelicat());
        harness.addToBattlefield(player1, new Gaelicat());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new PrishesWanderings(), "{2}{G}");
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
