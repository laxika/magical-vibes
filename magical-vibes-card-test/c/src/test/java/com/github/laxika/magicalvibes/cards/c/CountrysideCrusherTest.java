package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.r.RusticClachan;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CountrysideCrusher.class, RusticClachan.class, ElvishWarrior.class})
class CountrysideCrusherTest extends BaseCardTest {

    private Permanent addCrusher(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CountrysideCrusher());
    }

    @Test
    @DisplayName("Upkeep reveals lands, bins them, and gains a +1/+1 counter for each land binned")
    void upkeepBinsConsecutiveLandsAndGainsCounterPerLand() {
        Permanent crusher = addCrusher(player1);
        ElvishWarrior nonland = new ElvishWarrior();
        harness.setLibrary(player1, List.of(new RusticClachan(), new RusticClachan(), nonland));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        // Both lands were put into the graveyard, the non-land stayed on top of the library.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);

        // One +1/+1 counter per land binned.
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep stops immediately on a non-land top card and gains no counters")
    void upkeepStopsOnNonlandTopCard() {
        Permanent crusher = addCrusher(player1);
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new RusticClachan()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Upkeep with an empty library does nothing and gains no counters")
    void upkeepWithEmptyLibraryDoesNothing() {
        Permanent crusher = addCrusher(player1);
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("A land binned into the controller's graveyard while a single land is on top gains exactly one counter")
    void upkeepBinsSingleLandForOneCounter() {
        Permanent crusher = addCrusher(player1);
        harness.setLibrary(player1, List.of(new RusticClachan(), new ElvishWarrior(), new RusticClachan()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A land put into your graveyard from the battlefield triggers the counter ability")
    void battlefieldLandPutIntoGraveyardGainsCounter() {
        Permanent crusher = addCrusher(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RusticClachan());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Rustic Clachan");
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void discardedLandTriggersSeparatelyFromReinforce() {
        Permanent crusher = addCrusher(player1);
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new RusticClachan()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, warrior.getId());
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Rustic Clachan");
        resolveAllTriggers();

        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsLandDoesNotTriggerCrusher() {
        Permanent crusher = addCrusher(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RusticClachan());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Rustic Clachan");
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsUpkeepDoesNotRevealControllersLibrary() {
        Permanent crusher = addCrusher(player1);
        RusticClachan land = new RusticClachan();
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void allLandLibraryIsEmptiedWithoutAttemptingToDraw() {
        Permanent crusher = addCrusher(player1);
        RusticClachan first = new RusticClachan();
        RusticClachan second = new RusticClachan();
        harness.setLibrary(player1, List.of(first, second));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(crusher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
