package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HedgewitchsMask.class, GrizzlyBears.class, HillGiant.class, CrawWurm.class})
class HedgewitchsMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Hedgewitch's Mask to a creature")
    void equipAttachesToCreature() {
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        mask.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature cannot be blocked by a creature with power 4 or greater")
    void cannotBeBlockedByCreatureWithPowerFourOrGreater() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        mask.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new CrawWurm());
        creature.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipped creature can be blocked by a creature with power less than 4")
    void canBeBlockedByCreatureWithPowerLessThanFour() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        mask.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        creature.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A blocker boosted to exactly four power cannot block the equipped creature")
    void blockerWithExactlyFourEffectivePowerCannotBlock() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        mask.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent blockerMask = addCreatureReady(player2, new HedgewitchsMask());
        blockerMask.setAttachedTo(blocker.getId());
        creature.setAttacking(true);
        prepareDeclareBlockers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unattached Mask does not boost or protect other creatures")
    void unattachedMaskHasNoEffectOnOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HedgewitchsMask());
        Permanent blocker = addCreatureReady(player2, new CrawWurm());
        creature.setAttacking(true);
        prepareDeclareBlockers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Re-equipping moves both the boost and blocking restriction to the new creature")
    void reEquippingMovesBothEffects() {
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        Permanent oldCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent newCreature = addCreatureReady(player1, new GrizzlyBears());
        mask.setAttachedTo(oldCreature.getId());
        Permanent blocker = addCreatureReady(player2, new CrawWurm());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newCreature)).isEqualTo(3);
        oldCreature.setAttacking(true);
        newCreature.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 2))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip requires two mana")
    void cannotEquipWithOnlyOneMana() {
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mask.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mask.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent mask = addCreatureReady(player1, new HedgewitchsMask());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mask.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
