package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BrazenBuccaneers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowedCaravel.class, BrazenBuccaneers.class, Forest.class})
class ShadowedCaravelTest extends BaseCardTest {

    @Test
    @DisplayName("Explore with land puts a +1/+1 counter on Shadowed Caravel")
    void exploreLandPutsCounter() {
        Permanent caravel = addCaravelReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // Explore trigger resolves automatically (no target needed for PutCountersOnSelfEffect)
        harness.passBothPriorities();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land (accept graveyard) puts a +1/+1 counter on Shadowed Caravel")
    void exploreNonLandAcceptPutsCounter() {
        Permanent caravel = addCaravelReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new BrazenBuccaneers());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the counter trigger
        harness.passBothPriorities();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land (decline, keep on top) puts a +1/+1 counter on Shadowed Caravel")
    void exploreNonLandDeclinePutsCounter() {
        Permanent caravel = addCaravelReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new BrazenBuccaneers());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the counter trigger
        harness.passBothPriorities();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple explores accumulate +1/+1 counters")
    void multipleExploresAccumulateCounters() {
        Permanent caravel = addCaravelReady(player1);

        // First explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Second explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Explore with empty library still puts a counter")
    void exploreEmptyLibraryPutsCounter() {
        Permanent caravel = addCaravelReady(player1);

        harness.setLibrary(player1, List.of());

        castExplorerAndResolveExplore();
        harness.passBothPriorities();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Caravel is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent caravel = addCaravelReady(player1);

        assertThat(gqs.isCreature(gd, caravel)).isFalse();
    }

    @Test
    @DisplayName("Crewing with a creature of power >= 2 animates Caravel")
    void crewWithSufficientPower() {
        Permanent caravel = addCaravelReady(player1);
        Permanent crew = addCreatureReady(player1, new BrazenBuccaneers()); // 2/2

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(caravel.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, caravel)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters boost effective power/toughness when crewed")
    void countersBoostedWhenCrewedAndAnimated() {
        Permanent caravel = addCaravelReady(player1);

        // Get a +1/+1 counter from explore
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Choose between the explorer and the added crew creature.
        Permanent crew = addCreatureReady(player1, new BrazenBuccaneers());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        // Base 2/2 + one +1/+1 counter = 3/3
        assertThat(gqs.getEffectivePower(gd, caravel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, caravel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addCaravelReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("An opponent exploring does not trigger Caravel")
    void opponentExploringDoesNotPutCounter() {
        Permanent caravel = addCaravelReady(player1);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player2, new BrazenBuccaneers());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Caravel gets its own counter for an allied explore")
    void multipleCaravelsEachGetCounter() {
        Permanent first = addCaravelReady(player1);
        Permanent second = addCaravelReady(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        castExplorerAndResolveExplore();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick creature can crew without tapping Caravel")
    void summoningSickCreatureCanCrew() {
        Permanent caravel = addCaravelReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BrazenBuccaneers());
        crew.setSummoningSick(true);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crew.isTapped()).isTrue();
        assertThat(caravel.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, caravel)).isTrue();
    }

    @Test
    @DisplayName("Crew animation ends at end of turn while counters remain")
    void crewEndsAtEndOfTurnAndCountersRemain() {
        Permanent caravel = addCaravelReady(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        castExplorerAndResolveExplore();
        harness.passBothPriorities();
        // The Buccaneers are the only creature, so crew payment is automatic.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, caravel)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, caravel)).isFalse();
        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addCaravelReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ShadowedCaravel());
        perm.setSummoningSick(false);
        return perm;
    }

    private void castExplorerAndResolveExplore() {
        harness.setHand(player1, List.of(new BrazenBuccaneers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }
}
