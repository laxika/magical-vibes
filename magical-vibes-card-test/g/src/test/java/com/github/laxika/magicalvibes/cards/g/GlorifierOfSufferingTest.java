package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.d.DeconstructionHammer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlorifierOfSuffering.class, ArmoredKincaller.class, DeconstructionHammer.class})
class GlorifierOfSufferingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts counters on up to two target creatures")
    void sacrificesCreatureAndCountersTwoTargets() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());

        castGlorifier();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Armored Kincaller");
    }

    @Test
    @DisplayName("Sacrificing an artifact puts a counter on one chosen creature")
    void sacrificesArtifactAndCountersOneTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DeconstructionHammer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());

        castGlorifier();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Deconstruction Hammer");
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());

        castGlorifier();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(sacrifice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The sacrifice can be made while choosing zero counter targets")
    void sacrificesWithZeroTargets() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DeconstructionHammer());

        castGlorifier();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deconstruction Hammer");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Glorifier and an opponent's creature can both receive counters")
    void targetsItselfAndOpponentsCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DeconstructionHammer());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new ArmoredKincaller());

        castGlorifier();
        var sourceId = harness.getPermanentId(player1, "Glorifier of Suffering");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(sourceId, opposingTarget.getId()));

        assertThat(opposingTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(opposingTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getId().equals(sourceId))
                .singleElement().satisfies(p ->
                        assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Accepting with no other permanent does not sacrifice Glorifier")
    void cannotSacrificeItself() {
        castGlorifier();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Glorifier of Suffering");
        harness.assertNotInGraveyard(player1, "Glorifier of Suffering");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("The counters' targets are chosen when the reflexive ability triggers, after the sacrifice")
    void targetsAreChosenWhenTheReflexiveAbilityTriggers() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DeconstructionHammer());
        harness.castFromHand(player1, new GlorifierOfSuffering(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).singleElement().satisfies(entry -> assertThat(entry.getTargetIds()).isEmpty());

        Permanent latecomer = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(latecomer.getId()));

        assertThat(latecomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getTargetIds()).containsExactly(latecomer.getId()));
        harness.passBothPriorities();

        assertThat(latecomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Deconstruction Hammer");
    }

    private void castGlorifier() {
        harness.castFromHand(player1, new GlorifierOfSuffering(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
