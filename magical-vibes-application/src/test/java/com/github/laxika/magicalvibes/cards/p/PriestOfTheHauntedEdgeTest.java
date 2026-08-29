package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriestOfTheHauntedEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability gives target creature -X/-X for controlled snow lands")
    void sacrificeAbilityUsesControlledSnowLandCount() {
        addReadyPriest(player1);
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Priest of the Haunted Edge");
    }

    @Test
    @DisplayName("With no snow lands, the ability gives -0/-0")
    void noSnowLandsGiveNoDebuff() {
        addReadyPriest(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        addReadyPriest(player1);
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    void cannotActivateAtInstantSpeed() {
        addReadyPriest(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a non-creature")
    void cannotTargetNonCreature() {
        addReadyPriest(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyPriest(Player player) {
        Permanent priest = new Permanent(new PriestOfTheHauntedEdge());
        priest.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(priest);
        return priest;
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
