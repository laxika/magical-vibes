package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AccessTunnel.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class})
class AccessTunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Access Tunnel adds one colorless mana")
    void tapsForColorlessMana() {
        addCreatureReady(player1, new AccessTunnel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Target creature with power 3 or less can't be blocked this turn")
    void makesPowerThreeCreatureUnblockable() {
        addCreatureReady(player1, new AccessTunnel());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());

        addAbilityMana();
        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        addAbilityMana();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetLargeCreature() {
        addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player2, new CrawWurm());

        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    void canTargetOpponentsCreatureAndPaysCostsImmediately() {
        Permanent tunnel = addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(tunnel.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void cannotActivateWithOnlyTwoMana() {
        Permanent tunnel = addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tunnel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreatureLand() {
        addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player2, new AccessTunnel());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetBecomesIllegalWhenPowerExceedsThreeBeforeResolution() {
        addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player1, new HillGiant());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void powerIncreaseAfterResolutionDoesNotRemoveUnblockability() {
        addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new CrawWurm());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(1);
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void cannotTargetCreatureWhoseEffectivePowerAlreadyExceedsThree() {
        addCreatureReady(player1, new AccessTunnel());
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setPowerModifier(1);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }
}
