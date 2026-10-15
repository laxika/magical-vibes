package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasiliskGate.class, SolemnSimulacrum.class})
class BasiliskGateTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureByTheNumberOfGatesControlled() {
        Permanent gate = addReadyBasiliskGate(player1);
        harness.addToBattlefield(player1, new BasiliskGate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void boostWearsOffAtCleanup() {
        addReadyBasiliskGate(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SolemnSimulacrum());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void abilityCannotTargetALand() {
        addReadyBasiliskGate(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null,
                gd.playerBattlefields.get(player1.getId()).get(0).getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBasiliskGate(Player player) {
        Permanent gate = harness.addToBattlefieldAndReturn(player, new BasiliskGate());
        gate.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gate;
    }

    @Test
    @DisplayName("Tapping Basilisk Gate adds colorless mana")
    void tapsForColorlessMana() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new BasiliskGate());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boosts a target creature by the number of Gates you control")
    void boostsByControlledGateCount() {
        harness.addToBattlefield(player1, new BasiliskGate());
        harness.addToBattlefield(player1, new BasiliskGate());
        harness.addToBattlefield(player1, new BasiliskGate());
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent-controlled Gates do not increase the boost")
    void countsOnlyGatesYouControl() {
        harness.addToBattlefield(player1, new BasiliskGate());
        harness.addToBattlefield(player2, new BasiliskGate());
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost ability can only be activated at sorcery speed")
    void boostAbilityRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new BasiliskGate());
        Permanent target = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void usesGateCountAtResolutionAndDoesNotRecalculateAfterward() {
        addReadyBasiliskGate(player1);
        Permanent otherGate = harness.addToBattlefieldAndReturn(player1, new BasiliskGate());
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(otherGate);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.addToBattlefield(player1, new BasiliskGate());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void resolvesWithZeroBoostWhenTheOnlyGateLeavesBeforeResolution() {
        Permanent gate = addReadyBasiliskGate(player1);
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gate);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void stillBoostsUsingRemainingGatesWhenTheSourceLeaves() {
        Permanent source = addReadyBasiliskGate(player1);
        harness.addToBattlefield(player1, new BasiliskGate());
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void cannotActivateBoostOutsideMainPhase() {
        Permanent gate = addReadyBasiliskGate(player1);
        Permanent target = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
        assertThat(gate.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void cannotActivateBoostWhileAnotherAbilityIsOnTheStack() {
        addReadyBasiliskGate(player1);
        Permanent secondGate = harness.addToBattlefieldAndReturn(player1, new BasiliskGate());
        Permanent target = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(secondGate.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new SolemnSimulacrum());
    }
}
