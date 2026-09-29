package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
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

@CardUsed({BasiliskGate.class, GrizzlyBears.class, RakdosGuildgate.class})
class BasiliskGateTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureByTheNumberOfGatesControlled() {
        Permanent gate = addReadyBasiliskGate(player1);
        harness.addToBattlefield(player1, new RakdosGuildgate());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
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
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
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
        harness.addToBattlefield(player2, new RakdosGuildgate());
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
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
