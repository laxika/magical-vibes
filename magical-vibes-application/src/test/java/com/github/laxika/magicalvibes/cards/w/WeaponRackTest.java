package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AnimatingFaerie;
import com.github.laxika.magicalvibes.cards.b.BringToLife;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeaponRack.class, RovingKeep.class, AnimatingFaerie.class, BringToLife.class,
        HardenedScales.class})
class WeaponRackTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new WeaponRack()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent rack = findPermanent(player1, "Weapon Rack");

        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Moves a +1/+1 counter onto target creature")
    void movesCounterOntoTargetCreature() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rack.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new WeaponRack());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WeaponRack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canMoveCounterOntoOpponentsCreature() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canActivateWithoutCountersButDoesNotPlaceOne() {
        Permanent rack = harness.addToBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(rack.isTapped()).isTrue();
        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPlaceCounterIfSourceLeavesBeforeResolution() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rack);
        gd.playerGraveyards.get(player1.getId()).add(rack.getCard());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPlaceCounterIfLastCounterIsRemovedBeforeResolution() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId());
        rack.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotRemoveCounterIfTargetLeavesBeforeResolution() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rack.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithSpellOnStack() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WeaponRack()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rack.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    void animatedRackCannotMoveCounterOntoItselfEvenWithHardenedScales() {
        Permanent rack = harness.enterBattlefieldAndReturn(player1, new WeaponRack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AnimatingFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAdventure(player1, 0, rack.getId());
        harness.passBothPriorities();
        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        rack.setSummoningSick(false);
        harness.addToBattlefield(player1, new HardenedScales());

        harness.activateAbility(player1, 0, null, rack.getId());
        harness.passBothPriorities();

        assertThat(rack.isTapped()).isTrue();
        assertThat(rack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }
}
