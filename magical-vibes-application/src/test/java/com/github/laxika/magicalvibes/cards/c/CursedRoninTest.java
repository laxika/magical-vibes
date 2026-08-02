package com.github.laxika.magicalvibes.cards.c;

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

class CursedRoninTest extends BaseCardTest {

    @Test
    @DisplayName("Cursed Ronin gets +1/+1 until end of turn when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent ronin = addReadyRonin(player1);
        ronin.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cursed Ronin gets +1/+1 until end of turn when it blocks")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);
        Permanent ronin = addReadyRonin(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ronin.getPowerModifier()).isEqualTo(1);
        assertThat(ronin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying {B} gives +1/+1 until end of turn, stacking across activations")
    void activatedAbilityPumps() {
        Permanent ronin = addCreatureReady(player1, new CursedRonin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(3);
    }

    @Test
    @DisplayName("The pump from the activated ability wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent ronin = addCreatureReady(player1, new CursedRonin());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ronin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ronin)).isEqualTo(1);
    }

    private Permanent addReadyRonin(Player player) {
        Permanent permanent = new Permanent(new CursedRonin());
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
