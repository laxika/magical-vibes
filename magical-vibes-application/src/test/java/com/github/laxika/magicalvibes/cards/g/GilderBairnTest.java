package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GilderBairnTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles +1/+1 counters on the target permanent (3 becomes 6) and untaps the source")
    void doublesPlusOnePlusOneCounters() {
        Permanent bairn = addReadyBairn(player1);
        Permanent bears = addReadyCreature(player1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        // Grizzly Bears 2/2 base + six +1/+1 counters.
        assertThat(bears.getEffectivePower()).isEqualTo(8);
        assertThat(bears.getEffectiveToughness()).isEqualTo(8);
        // Paying {Q} untapped the source.
        assertThat(bairn.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Doubles every kind of counter present on the target")
    void doublesEachKindOfCounter() {
        addReadyBairn(player1);
        Permanent bears = addReadyCreature(player1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does nothing when the target permanent has no counters")
    void noOpWhenTargetHasNoCounters() {
        addReadyBairn(player1);
        Permanent bears = addReadyCreature(player1); // no counters
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it tapped)")
    void cannotActivateWhileUntapped() {
        Permanent bairn = new Permanent(new GilderBairn());
        bairn.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bairn); // left untapped
        Permanent bears = addReadyCreature(player1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareTurn();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
    }

    private Permanent addReadyBairn(Player player) {
        Permanent perm = new Permanent(new GilderBairn());
        perm.setSummoningSick(false);
        perm.tap(); // {Q} requires the source to be tapped
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
