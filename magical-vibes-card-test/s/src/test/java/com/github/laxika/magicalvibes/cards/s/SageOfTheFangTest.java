package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({SageOfTheFang.class, Mountain.class})
class SageOfTheFangTest extends BaseCardTest {

    @Test
    @DisplayName("When Sage of the Fang enters, it puts a +1/+1 counter on target creature")
    void entersWithTargetCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SageOfTheFang());
        harness.setHand(player1, List.of(new SageOfTheFang()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Renew puts a counter on target creature, doubles its counters, and exiles Sage of the Fang")
    void renewPutsAndDoublesCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SageOfTheFang());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Sage of the Fang");
    }

    @Test
    @DisplayName("Renew requires a creature target")
    void renewRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Renew can only be activated as a sorcery")
    void renewIsSorcerySpeedOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SageOfTheFang());
        harness.setGraveyard(player1, List.of(new SageOfTheFang()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersCanTargetItself() {
        Permanent sage = harness.enterBattlefieldAndReturn(player1, new SageOfTheFang());

        harness.handlePermanentChosen(player1, sage.getId());
        harness.passBothPriorities();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SageOfTheFang());
        harness.enterBattlefieldAndReturn(player1, new SageOfTheFang());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void renewDoublesExistingCountersAfterAddingOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SageOfTheFang());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.setCounterCount(CounterType.STUN, 2);
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void renewCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SageOfTheFang());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void renewExilesSourceBeforeResolutionAndStillCostsItWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SageOfTheFang());
        readyRenew();
        SageOfTheFang source = (SageOfTheFang) gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        harness.assertNotInGraveyard(player1, "Sage of the Fang");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void renewCannotActivateOutsideMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SageOfTheFang());
        readyRenew();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Sage of the Fang");
    }

    @Test
    void renewCannotActivateWithSpellOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SageOfTheFang());
        readyRenew();
        harness.setHand(player1, List.of(new SageOfTheFang()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Sage of the Fang");
    }

    private void readyRenew() {
        harness.setGraveyard(player1, List.of(new SageOfTheFang()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
