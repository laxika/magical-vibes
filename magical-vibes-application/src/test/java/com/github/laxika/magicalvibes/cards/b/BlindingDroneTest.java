package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlindingDrone.class, GrizzlyBears.class, Forest.class})
class BlindingDroneTest extends BaseCardTest {

    @Test
    void paysColorlessManaAndTapsTargetCreature() {
        Permanent drone = addCreatureReady(player1, new BlindingDrone());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent drone = addCreatureReady(player1, new BlindingDrone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(drone.isTapped()).isFalse();
    }

    @Test
    void coloredManaCannotPayColorlessCost() {
        Permanent drone = addCreatureReady(player1, new BlindingDrone());
        Permanent target = addCreatureReady(player2, new BlindingDrone());
        for (ManaColor color : new ManaColor[]{ManaColor.WHITE, ManaColor.BLUE,
                ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN}) {
            harness.addMana(player1, color, 1);
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(drone.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickDroneCannotActivateTapAbility() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new BlindingDrone());
        Permanent target = addCreatureReady(player2, new BlindingDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(drone.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tappedDroneCannotActivateTapAbility() {
        Permanent drone = addCreatureReady(player1, new BlindingDrone());
        drone.tap();
        Permanent target = addCreatureReady(player2, new BlindingDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void canTapFriendlyCreatureAndTargetIsNotTappedUntilResolution() {
        Permanent drone = addCreatureReady(player1, new BlindingDrone());
        Permanent target = addCreatureReady(player1, new BlindingDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(drone.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItselfEvenThoughTapCostTapsItBeforeResolution() {
        Permanent drone = addCreatureReady(player1, new BlindingDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, drone.getId());
        harness.passBothPriorities();

        assertThat(drone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent drone = addCreatureReady(player1, new BlindingDrone());
        Permanent target = addCreatureReady(player2, new BlindingDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(drone);
        gd.playerGraveyards.get(player1.getId()).add(drone.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
