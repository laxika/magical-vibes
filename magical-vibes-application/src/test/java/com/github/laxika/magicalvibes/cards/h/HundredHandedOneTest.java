package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

class HundredHandedOneTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Hundred-Handed One")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent hundredHandedOne = addReadyHundredHandedOne(player1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hundredHandedOne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hundredHandedOne.isMonstrous()).isTrue();
        assertThat(hundredHandedOne.getEffectivePower()).isEqualTo(6);
        assertThat(hundredHandedOne.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Monstrous Hundred-Handed One has reach and can block two attackers")
    void monstrousAbilitiesApply() {
        Permanent hundredHandedOne = addReadyHundredHandedOne(player2);
        activateMonstrosity(hundredHandedOne);

        assertThat(gqs.hasKeyword(gd, hundredHandedOne, Keyword.REACH)).isTrue();

        addAttacker(player1);
        addAttacker(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(hundredHandedOne);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ));

        assertThat(hundredHandedOne.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Monstrosity cannot be activated again after it resolves")
    void monstrosityOnlyResolvesOnce() {
        Permanent hundredHandedOne = addReadyHundredHandedOne(player1);
        activateMonstrosity(hundredHandedOne);
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyHundredHandedOne(Player player) {
        Permanent permanent = new Permanent(new HundredHandedOne());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void activateMonstrosity(Permanent hundredHandedOne) {
        Player controller = gd.playerBattlefields.get(player1.getId()).contains(hundredHandedOne) ? player1 : player2;
        int index = gd.playerBattlefields.get(controller.getId()).indexOf(hundredHandedOne);
        harness.forceActivePlayer(controller);
        harness.addMana(controller, ManaColor.WHITE, 6);
        harness.activateAbility(controller, index, null, null);
        harness.passBothPriorities();
    }

    private void addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
    }
}
