package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumaiOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido 2 triggers when Numai Outcast becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent outcast = addReadyOutcast(player1);
        outcast.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(outcast.getPowerModifier()).isEqualTo(2);
        assertThat(outcast.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido 2 triggers when Numai Outcast blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent outcast = addReadyOutcast(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(outcast.getPowerModifier()).isEqualTo(2);
        assertThat(outcast.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The bushido bonus wears off at end of turn")
    void bushidoWearsOffAtEndOfTurn() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent outcast = addReadyOutcast(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(outcast.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(outcast.getPowerModifier()).isEqualTo(0);
        assertThat(outcast.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Paying {B} and 5 life grants a regeneration shield")
    void payManaAndLifeGrantsRegenerationShield() {
        Permanent outcast = addCreatureReady(player1, new NumaiOutcast());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(outcast.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability with less than 5 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new NumaiOutcast());
        harness.setLife(player1, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("The regeneration shield saves Numai Outcast from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent outcast = addCreatureReady(player1, new NumaiOutcast());
        outcast.setRegenerationShield(1);
        outcast.setBlocking(true);
        outcast.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Numai Outcast");
        assertThat(outcast.isTapped()).isTrue();
        assertThat(outcast.getRegenerationShield()).isEqualTo(0);
    }

    private Permanent addReadyOutcast(Player player) {
        Permanent permanent = new Permanent(new NumaiOutcast());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
