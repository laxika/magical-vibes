package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WreckingBallArm.class, CaptainSisay.class, GrizzlyBears.class, HillGiant.class})
class WreckingBallArmTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has base power and toughness 7/7")
    void equippedCreatureBecomesSevenSeven() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent arm = addArmReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        arm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Equipped creature cannot be blocked by creatures with power 2 or less")
    void equippedCreatureCannotBeBlockedBySmallCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent arm = addArmReady(player1);
        arm.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Equipped creature can be blocked by a creature with power greater than 2")
    void equippedCreatureCanBeBlockedByLargerCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent arm = addArmReady(player1);
        arm.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Three-mana equip can target a legendary creature")
    void threeManaEquipTargetsLegendaryCreature() {
        Permanent arm = addArmReady(player1);
        Permanent legendaryCreature = addCreatureReady(player1, new CaptainSisay());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(arm.getAttachedTo()).isEqualTo(legendaryCreature.getId());
    }

    @Test
    @DisplayName("Three-mana equip cannot target a nonlegendary creature")
    void threeManaEquipRejectsNonlegendaryCreature() {
        addArmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Seven-mana equip can target a nonlegendary creature")
    void sevenManaEquipTargetsNonlegendaryCreature() {
        Permanent arm = addArmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(arm.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addArmReady(Player player) {
        return addCreatureReady(player, new WreckingBallArm());
    }

    @Test
    void countersApplyOnTopOfSevenSevenBaseStats() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent arm = addArmReady(player1);
        arm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    void blockerWithPowerRaisedAboveTwoCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent arm = addArmReady(player1);
        arm.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reequippingRestoresOldCreaturesStatsAndBlocking() {
        Permanent arm = addArmReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent newCreature = addCreatureReady(player1, new HillGiant());
        arm.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, newCreature.getId());
        harness.passBothPriorities();

        assertThat(arm.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, newCreature)).isEqualTo(7);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oldCreature);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void neitherEquipAbilityCanTargetAnOpponentsLegendaryCreature() {
        addArmReady(player1);
        Permanent creature = addCreatureReady(player2, new CaptainSisay());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
    }
}
