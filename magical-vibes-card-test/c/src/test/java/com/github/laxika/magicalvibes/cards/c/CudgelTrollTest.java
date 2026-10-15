package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CudgelTroll.class, RuneclawBear.class})
class CudgelTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new CudgelTroll());
        troll.setSummoningSick(true);
        troll.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting Cudgel Troll puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new CudgelTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cudgel Troll");
    }

    @Test
    @DisplayName("Resolving Cudgel Troll puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new CudgelTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cudgel Troll");
    }

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent trollPerm = addCudgelTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(trollPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCudgelTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCudgelTrollReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can stack multiple regeneration shields")
    void canStackMultipleRegenerationShields() {
        addCudgelTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(troll.getRegenerationShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Regeneration shield saves Cudgel Troll from lethal combat damage when blocking")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent trollPerm = addCudgelTrollReady(player1);
        trollPerm.setRegenerationShield(1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CudgelTroll());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cudgel Troll");
        Permanent troll = findPermanent(player1, "Cudgel Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
        assertThat(troll.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Cudgel Troll dies without regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShieldInCombat() {
        Permanent trollPerm = addCudgelTrollReady(player1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CudgelTroll());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cudgel Troll");
        harness.assertInGraveyard(player1, "Cudgel Troll");
    }

    @Test
    @DisplayName("Cudgel Troll survives combat damage below its toughness")
    void survivesNonLethalCombatDamage() {
        Permanent trollPerm = addCudgelTrollReady(player1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cudgel Troll");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Creating a shield does not tap or heal the creature until destruction")
    void shieldWaitsForDestructionAndClearsAllDamage() {
        Permanent troll = addCudgelTrollReady(player1);
        troll.setMarkedDamage(2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.isTapped()).isFalse();
        assertThat(troll.getMarkedDamage()).isEqualTo(2);
        troll.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Cudgel Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);

        troll.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Cudgel Troll");
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isZero();

        troll.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Cudgel Troll");
        harness.assertInGraveyard(player1, "Cudgel Troll");
    }

    private Permanent addCudgelTrollReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CudgelTroll());
        perm.setSummoningSick(false);
        return perm;
    }
}
