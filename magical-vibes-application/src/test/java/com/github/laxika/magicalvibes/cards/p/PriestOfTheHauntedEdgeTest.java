package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriestOfTheHauntedEdge.class, AirElemental.class, SnowCoveredSwamp.class, Swamp.class})
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

    @Test
    void snowLandsAreCountedAtResolutionAndAmountThenStaysFixed() {
        addReadyPriest(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredSwamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PriestOfTheHauntedEdge());
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Priest of the Haunted Edge");
        harness.assertInGraveyard(player1, "Priest of the Haunted Edge");
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void snowCreaturesDoNotCountAndOwnCreaturesCanBeTargeted() {
        addReadyPriest(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PriestOfTheHauntedEdge());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void reducingToughnessToZeroPutsCreatureInGraveyard() {
        addReadyPriest(player1);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SnowCoveredSwamp());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PriestOfTheHauntedEdge());
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Priest of the Haunted Edge");
        harness.assertInGraveyard(player2, "Priest of the Haunted Edge");
    }

    @Test
    void summoningSicknessPreventsActivationWithoutSacrificingPriest() {
        harness.addToBattlefield(player1, new PriestOfTheHauntedEdge());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PriestOfTheHauntedEdge());
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Priest of the Haunted Edge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedPriestCannotActivate() {
        Permanent priest = addReadyPriest(player1);
        priest.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PriestOfTheHauntedEdge());
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Priest of the Haunted Edge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addReadyPriest(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PriestOfTheHauntedEdge());
        forceMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Priest of the Haunted Edge");
    }

    @Test
    void cannotActivateWithAnotherAbilityOnStack() {
        addReadyPriest(player1);
        addReadyPriest(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PriestOfTheHauntedEdge());
        forceMainPhase(player1);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void abilityDoesNotAffectTargetThatLeavesAndReturns() {
        addReadyPriest(player1);
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        PriestOfTheHauntedEdge targetCard = new PriestOfTheHauntedEdge();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        forceMainPhase(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.passBothPriorities();

        assertThat(returned.getPowerModifier()).isZero();
        assertThat(returned.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Priest of the Haunted Edge");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPriest(Player player) {
        Permanent priest = harness.addToBattlefieldAndReturn(player, new PriestOfTheHauntedEdge());
        priest.setSummoningSick(false);
        return priest;
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
