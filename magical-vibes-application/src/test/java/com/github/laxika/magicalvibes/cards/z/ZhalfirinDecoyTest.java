package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhalfirinDecoy.class, GrizzlyBears.class, Forest.class})
class ZhalfirinDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature after a creature enters under your control")
    void tapsTargetCreatureAfterCreatureEnters() {
        Permanent decoy = addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(decoy.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a creature entering under your control this turn")
    void cannotActivateWithoutCreatureEnteringThisTurn() {
        addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void opponentsCreatureEnteringDoesNotPermitActivation() {
        Permanent decoy = addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new ZhalfirinDecoy());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
        assertThat(decoy.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void landEnteringDoesNotPermitActivation() {
        Permanent decoy = addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent target = addCreatureReady(player2, new ZhalfirinDecoy());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
        assertThat(decoy.isTapped()).isFalse();
    }

    @Test
    void enteringCreatureNeedNotRemainOnBattlefield() {
        Permanent decoy = addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent target = addCreatureReady(player2, new ZhalfirinDecoy());
        Permanent entered = harness.enterBattlefieldAndReturn(player1, new ZhalfirinDecoy());
        entered.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(entered);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(decoy.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void ownEntrySatisfiesConditionButSummoningSicknessPreventsActivation() {
        Permanent decoy = harness.enterBattlefieldAndReturn(player1, new ZhalfirinDecoy());
        Permanent target = addCreatureReady(player2, new ZhalfirinDecoy());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(decoy.isTapped()).isFalse();

        decoy.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(decoy.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetOwnAlreadyTappedCreature() {
        Permanent decoy = addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent target = harness.enterBattlefieldAndReturn(player1, new ZhalfirinDecoy());
        target.setTapped(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(decoy.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void previousTurnsCreatureEntryDoesNotPermitActivation() {
        Permanent decoy = addCreatureReady(player1, new ZhalfirinDecoy());
        Permanent target = addCreatureReady(player2, new ZhalfirinDecoy());
        harness.enterBattlefieldAndReturn(player1, new ZhalfirinDecoy());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
        assertThat(decoy.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }
}
