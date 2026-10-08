package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampireHexmage.class, GrizzlyBears.class, BurstOfStrength.class})
class VampireHexmageTest extends BaseCardTest {

    @BeforeEach
    void mainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Sacrificing it removes every counter from target permanent")
    void removesAllCountersFromTargetPermanent() {
        Permanent hexmage = addCreatureReady(player1, new VampireHexmage());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hexmage);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The target permanent can receive counters again after the ability resolves")
    void doesNotLockTargetAgainstFutureCounters() {
        addCreatureReady(player1, new VampireHexmage());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability only targets permanents")
    void rejectsPlayerTarget() {
        addCreatureReady(player1, new VampireHexmage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    @Test
    void canActivateWhileSummoningSick() {
        Permanent hexmage = harness.addToBattlefieldAndReturn(player1, new VampireHexmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VampireHexmage());
        target.setCounterCount(CounterType.CHARGE, 2);
        hexmage.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Hexmage");
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void canActivateWhileTapped() {
        Permanent hexmage = addCreatureReady(player1, new VampireHexmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VampireHexmage());
        target.setCounterCount(CounterType.CHARGE, 2);
        hexmage.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Hexmage");
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void sacrificesImmediatelyAndRemovesCountersPresentAtResolution() {
        Permanent hexmage = addCreatureReady(player1, new VampireHexmage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VampireHexmage());
        target.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hexmage);
        harness.assertInGraveyard(player1, "Vampire Hexmage");
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        target.setCounterCount(CounterType.CHARGE, 3);
        target.setCounterCount(CounterType.FLYING, 1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void canTargetPermanentWithoutCounters() {
        addCreatureReady(player1, new VampireHexmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VampireHexmage());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Hexmage");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItselfButSacrificeMakesTheTargetIllegalAtResolution() {
        Permanent hexmage = addCreatureReady(player1, new VampireHexmage());
        hexmage.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, null, hexmage.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vampire Hexmage");
        harness.assertInGraveyard(player1, "Vampire Hexmage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void firstStrikeKillsBlockerBeforeItCanDealDamage() {
        Permanent hexmage = addCreatureReady(player1, new VampireHexmage());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hexmage);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }
}
