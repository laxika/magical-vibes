package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LesserWerewolf.class, GrizzlyBears.class})
class LesserWerewolfTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -1/-0 and puts a -0/-1 counter on a creature blocking it")
    void weakensSelfAndCountersBlocker() {
        Permanent werewolf = addCreatureReady(player1, new LesserWerewolf());
        werewolf.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, werewolf))));

        activate(werewolf, blocker);

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(1);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Checks the power condition once before applying both effects")
    void stillCountersTargetWhenThePowerBecomesZero() {
        Permanent werewolf = addCreatureReady(player1, new LesserWerewolf());
        werewolf.setPowerModifier(-1);
        werewolf.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, werewolf))));

        activate(werewolf, blocker);

        assertThat(gqs.getEffectivePower(gd, werewolf)).isZero();
        assertThat(blocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when its power is zero")
    void doesNothingAtZeroPower() {
        Permanent werewolf = addCreatureReady(player1, new LesserWerewolf());
        werewolf.setPowerModifier(-2);
        werewolf.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, werewolf))));

        activate(werewolf, blocker);

        assertThat(gqs.getEffectivePower(gd, werewolf)).isZero();
        assertThat(blocker.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature not blocking or blocked by it")
    void cannotTargetUnrelatedCreature() {
        Permanent werewolf = addCreatureReady(player1, new LesserWerewolf());
        Permanent unrelated = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, werewolf), null, unrelated.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during the declare blockers step")
    void cannotActivateOutsideDeclareBlockers() {
        Permanent werewolf = addCreatureReady(player1, new LesserWerewolf());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, werewolf), null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declare blockers");
    }

    private void activate(Permanent werewolf, Permanent target) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(player1, werewolf), null, target.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
