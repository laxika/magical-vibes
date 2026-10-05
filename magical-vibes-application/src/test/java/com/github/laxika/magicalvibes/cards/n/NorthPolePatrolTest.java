package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TrustyBoomerang;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorthPolePatrol.class, OtterPenguin.class, Plains.class, TrustyBoomerang.class})
class NorthPolePatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps another permanent you control")
    void untapsAnotherPermanentYouControl() {
        Permanent patrol = addReadyPatrol();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Waterbend taps an opposing creature and the permanents used to pay")
    void waterbendTapsOpposingCreature() {
        Permanent patrol = addReadyPatrol();
        Permanent firstPayment = addReadyCreature(player1);
        Permanent secondPayment = addReadyCreature(player1);
        Permanent thirdPayment = addReadyCreature(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(firstPayment.isTapped()).isTrue();
        assertThat(secondPayment.isTapped()).isTrue();
        assertThat(thirdPayment.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend cannot target a creature you control")
    void waterbendCannotTargetYourCreature() {
        addReadyPatrol();
        addReadyCreature(player1);
        addReadyCreature(player1);
        addReadyCreature(player1);
        Permanent ownCreature = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private Permanent addReadyPatrol() {
        return addCreatureReady(player1, new NorthPolePatrol());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new OtterPenguin());
    }

    @Test
    void cannotUntapItself() {
        Permanent patrol = addReadyPatrol();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, patrol.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(patrol.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canUntapAnotherCopy() {
        Permanent patrol = addReadyPatrol();
        Permanent other = addReadyPatrol();
        other.tap();

        harness.activateAbility(player1, 0, null, other.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    void cannotUntapOpponentsPermanent() {
        Permanent patrol = addReadyPatrol();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(patrol.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void untapTargetThatChangesControllerIsIllegalOnResolution() {
        Permanent patrol = addReadyPatrol();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();
        harness.activateAbility(player1, 0, null, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void waterbendCanBePaidEntirelyWithMana() {
        Permanent patrol = addReadyPatrol();
        Permanent payment = addReadyCreature(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(patrol.isTapped()).isTrue();
        assertThat(payment.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void waterbendCanCombineManaAndSummoningSickCreatures() {
        Permanent patrol = addReadyPatrol();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OtterPenguin());
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(patrol.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void waterbendCannotCountSourceTwiceOrUseLands() {
        Permanent patrol = addReadyPatrol();
        Permanent first = addReadyCreature(player1);
        Permanent second = addReadyCreature(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(patrol.isTapped()).isFalse();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickPatrolCannotActivateEitherTapAbility() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NorthPolePatrol());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(patrol.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void waterbendCannotTargetOpponentsLand() {
        addReadyPatrol();
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void waterbendCanTapNoncreatureArtifactsForPayment() {
        Permanent patrol = addReadyPatrol();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TrustyBoomerang());
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void waterbendTargetThatBecomesControlledByYouIsIllegalOnResolution() {
        Permanent patrol = addReadyPatrol();
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void losingWaterbendTargetDoesNotRefundPayment() {
        Permanent patrol = addReadyPatrol();
        Permanent first = addReadyCreature(player1);
        Permanent second = addReadyCreature(player1);
        Permanent third = addReadyCreature(player1);
        Permanent target = addReadyCreature(player2);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
