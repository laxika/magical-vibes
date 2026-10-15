package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HogMonkeyRampage.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class HogMonkeyRampageTest extends BaseCardTest {

    @Test
    @DisplayName("A creature with power 4 or greater gets a counter before fighting")
    void putsCounterBeforeFightWhenPowerIsAtLeastFour() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(attacker, blocker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature with power less than 4 still fights without getting a counter")
    void fightsWithoutCounterWhenPowerIsLessThanFour() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(attacker, blocker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Targets must be one creature you control and one creature an opponent controls")
    void rejectsInvalidTargetControllers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HogMonkeyRampage()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opponentCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterLetsCreatureSurviveFightWithEqualPowerCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(attacker, blocker);

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void checksPowerAfterCastingRatherThanWhenChoosingTargets() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast(attacker, blocker);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void stillPlacesCounterWhenOpponentTargetLeavesBeforeResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast(attacker, blocker);
        gd.playerBattlefields.get(player2.getId()).remove(blocker);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Hog-Monkey Rampage");
    }

    @Test
    void doesNotFightWhenOwnTargetLeavesBeforeResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareCast(attacker, blocker);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Hog-Monkey Rampage");
    }

    private void cast(Permanent ownCreature, Permanent opponentCreature) {
        prepareManaAndHand();
        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
    }

    private void prepareCast(Permanent ownCreature, Permanent opponentCreature) {
        prepareManaAndHand();
        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
    }

    private void prepareManaAndHand() {
        harness.setHand(player1, List.of(new HogMonkeyRampage()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
