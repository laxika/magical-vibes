package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MutatedCultist.class, GrizzlyBears.class})
class MutatedCultistTest extends BaseCardTest {

    @Test
    void removesPermanentCountersAndReducesNextSpellByRemovedAmount() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 1);

        castCultist(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void removesAllTrackedCountersFromOpponent() {
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 4);
        gd.playerSparkCounters.put(player2.getId(), 1);
        gd.playerExperienceCounters.put(player2.getId(), 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);

        castCultist(player2.getId());

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerSparkCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerExperienceCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    void mayBeCastWithoutChoosingATarget() {
        castCultist(player1.getId());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canRemoveCountersFromItsControllersPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MutatedCultist());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castCultist(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void reductionIsConsumedByTheNextSpellOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        castCultist(target.getId());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    void reductionCannotPayColoredMana() {
        gd.playerEnergyCounters.put(player2.getId(), 4);
        castCultist(player2.getId());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void reductionEqualsTheNumberOfCountersActuallyRemoved() {
        gd.playerEnergyCounters.put(player2.getId(), 1);
        castCultist(player2.getId());

        harness.setHand(player1, List.of(new MutatedCultist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void unusedReductionExpiresAtEndOfTurn() {
        harness.setLibrary(player1, List.of(new MutatedCultist(), new MutatedCultist()));
        harness.setLibrary(player2, List.of(new MutatedCultist(), new MutatedCultist()));
        gd.playerEnergyCounters.put(player2.getId(), 4);
        castCultist(player2.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetLeavingBeforeResolutionGrantsNoReduction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new MutatedCultist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mutated Cultist");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCultist(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MutatedCultist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
