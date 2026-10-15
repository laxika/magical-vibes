package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Worldslayer.class, GoblinPiker.class, Forest.class})
class WorldslayerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player destroys every permanent except Worldslayer itself")
    void combatDamageWipesEverythingButWorldslayer() {
        Permanent creature = addCreatureReady(player1, new GoblinPiker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent enemy = addCreatureReady(player2, new GoblinPiker());
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        worldslayer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(worldslayer);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enemy);
    }

    @Test
    @DisplayName("No wipe when the equipped creature is blocked and deals no damage to a player")
    void noWipeWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GoblinPiker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        worldslayer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GoblinPiker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(worldslayer, land);
    }

    @Test
    @DisplayName("Equip ability costs five mana and attaches Worldslayer to a creature")
    void equipCostsFiveManaAndAttaches() {
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        Permanent creature = addCreatureReady(player1, new GoblinPiker());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(worldslayer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Only the triggering Worldslayer survives, not another copy")
    void destroysOtherWorldslayer() {
        Permanent creature = addCreatureReady(player1, new GoblinPiker());
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        harness.addToBattlefield(player2, new Worldslayer());
        worldslayer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(worldslayer);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An unattached Worldslayer does not trigger from another creature's combat damage")
    void unattachedEquipmentDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GoblinPiker());
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        creature.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature, worldslayer);
    }

    @Test
    @DisplayName("Detaching Worldslayer after combat damage does not stop its triggered ability")
    void triggerResolvesAfterDetaching() {
        Permanent creature = addCreatureReady(player1, new GoblinPiker());
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        worldslayer.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        worldslayer.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(worldslayer);
    }

    @Test
    @DisplayName("Worldslayer cannot equip an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        Permanent creature = addCreatureReady(player2, new GoblinPiker());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(worldslayer.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Worldslayer cannot equip during combat")
    void equipRequiresSorceryTiming() {
        Permanent worldslayer = harness.addToBattlefieldAndReturn(player1, new Worldslayer());
        Permanent creature = addCreatureReady(player1, new GoblinPiker());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(worldslayer.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }
}
