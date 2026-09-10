package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhaoZilongTigerGeneral.class, ForestBear.class, ShuCavalry.class})
class ZhaoZilongTigerGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent zhao = addZhaoReady(player2);

        addAttackerReady(player1);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(zhao.getId());
    }

    @Test
    @DisplayName("Resolving the block trigger gives +1/+1 until end of turn")
    void blockTriggerGivesPlusOnePlusOne() {
        Permanent zhao = addZhaoReady(player2);

        addAttackerReady(player1);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(zhao.getPowerModifier()).isEqualTo(1);
        assertThat(zhao.getToughnessModifier()).isEqualTo(1);
        assertThat(zhao.getEffectivePower()).isEqualTo(4);
        assertThat(zhao.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("+1/+1 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent zhao = addZhaoReady(player2);

        addAttackerReady(player1);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(zhao.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(zhao.getPowerModifier()).isEqualTo(0);
        assertThat(zhao.getToughnessModifier()).isEqualTo(0);
        assertThat(zhao.getEffectivePower()).isEqualTo(3);
        assertThat(zhao.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("No trigger fires when Zhao Zilong is not blocking")
    void noTriggerWhenNotBlocking() {
        addZhaoReady(player2);
        addAttackerReady(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(HighGround.class)
    @DisplayName("Blocking multiple creatures triggers only once")
    void blockTriggerFiresOnlyOnceWhenBlockingMultipleCreatures() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent zhao = addZhaoReady(player2);
        addAttackerReady(player1);
        addAttackerReady(player1);

        prepareDeclareBlockers();
        int zhaoIndex = gd.playerBattlefields.get(player2.getId()).indexOf(zhao);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(zhaoIndex, 0),
                new BlockerAssignment(zhaoIndex, 1)));

        assertThat(gd.stack.stream()
                .filter(entry -> zhao.getId().equals(entry.getSourcePermanentId())))
                .hasSize(1);
        resolveAllTriggers();

        assertThat(zhao.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Horsemanship prevents a creature without horsemanship from blocking Zhao Zilong")
    void horsemanshipPreventsNonHorsemanshipBlock() {
        Permanent blocker = addCreatureReady(player2, new ForestBear());
        Permanent zhao = addCreatureReady(player1, new ZhaoZilongTigerGeneral());
        zhao.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(zhao)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("A creature with horsemanship can block Zhao Zilong without triggering its block ability")
    void horsemanshipCreatureCanBlockWithoutZhaoTriggering() {
        Permanent blocker = addCreatureReady(player2, new ShuCavalry());
        Permanent zhao = addCreatureReady(player1, new ZhaoZilongTigerGeneral());
        zhao.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(zhao))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(zhao.getPowerModifier()).isZero();
        assertThat(zhao.getToughnessModifier()).isZero();
    }

    private Permanent addZhaoReady(Player player) {
        return addCreatureReady(player, new ZhaoZilongTigerGeneral());
    }

    private void addAttackerReady(Player player) {
        Permanent attacker = addCreatureReady(player, new ForestBear());
        attacker.setAttacking(true);
    }
}
