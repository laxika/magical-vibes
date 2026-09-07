package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({DrudgeSkeletons.class, GrizzlyBears.class, ProdigalSorcerer.class, RoyalAssassin.class,
        WrathOfGod.class})
class DrudgeSkeletonsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Drudge Skeletons puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new DrudgeSkeletons(), "{1}{B}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving Drudge Skeletons puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.castFromHand(player1, new DrudgeSkeletons(), "{1}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(skelePerm.getId());
    }

    @Test
    @DisplayName("Activating regeneration ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent skele = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(skele.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        Permanent skele = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(skele.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can stack multiple regeneration shields")
    void canStackMultipleRegenerationShields() {
        Permanent skele = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skele.getRegenerationShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate regeneration ability even when tapped")
    void canActivateWhenTapped() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate regeneration ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new DrudgeSkeletons());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating regeneration ability logs the activation")
    void activatingAbilityLogsActivation() {
        addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameLogContains("activates Drudge Skeletons's ability")).isTrue();
    }

    @Test
    @DisplayName("Resolving regeneration ability logs the shield")
    void resolvingAbilityLogsShield() {
        addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("gains a regeneration shield")).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if Drudge Skeletons is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Regeneration shield saves Drudge Skeletons from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        Permanent skele = findPermanent(player1, "Drudge Skeletons");
        assertThat(skele.isTapped()).isTrue();
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Drudge Skeletons dies without regeneration shield in combat")
    void diesWithoutRegenerationShieldInCombat() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Drudge Skeletons");
        harness.assertInGraveyard(player1, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Regeneration shield saves attacking Drudge Skeletons from lethal blocker damage")
    void regenerationSavesAttackingCreature() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);
        skelePerm.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        Permanent skele = findPermanent(player1, "Drudge Skeletons");
        assertThat(skele.isTapped()).isTrue();
        assertThat(skele.isAttacking()).isFalse();
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Regeneration logs that the creature regenerates")
    void regenerationLogsCombat() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gameLogContains("Drudge Skeletons regenerates")).isTrue();
    }

    @Test
    @DisplayName("Regeneration shield saves Drudge Skeletons from destruction")
    void regenerationSavesFromDestruction() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);
        skelePerm.tap();

        addCreatureReady(player2, new RoyalAssassin());
        harness.activateAbility(player2, 0, null, skelePerm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        harness.assertNotInGraveyard(player1, "Drudge Skeletons");
        assertThat(skelePerm.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shield saves Drudge Skeletons from lethal noncombat damage")
    void regenerationSavesFromNoncombatDamage() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);

        addCreatureReady(player2, new ProdigalSorcerer());
        declareAttackers(List.of(0));

        harness.activateAbility(player2, 0, null, skelePerm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        harness.assertNotInGraveyard(player1, "Drudge Skeletons");
        assertThat(skelePerm.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Without regeneration shield, direct damage kills Drudge Skeletons")
    void directDamageKillsWithoutShield() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());

        addCreatureReady(player2, new ProdigalSorcerer());
        declareAttackers(List.of(0));

        harness.activateAbility(player2, 0, null, skelePerm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Skeletons");
        harness.assertInGraveyard(player1, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Wrath of God destroys Drudge Skeletons even with regeneration shield")
    void wrathOfGodIgnoresRegenerationShield() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Skeletons");
        harness.assertInGraveyard(player1, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Regeneration shield clears at end of turn cleanup")
    void regenerationShieldClearsAtEndOfTurn() {
        Permanent skele = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skele.getRegenerationShield()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(skele.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Only one regeneration shield is consumed per lethal damage event")
    void onlyOneShieldConsumedPerLethalEvent() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(3);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        Permanent skele = findPermanent(player1, "Drudge Skeletons");
        assertThat(skele.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration clears blocking state")
    void regenerationClearsBlockingState() {
        Permanent skelePerm = addCreatureReady(player1, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        Permanent skele = findPermanent(player1, "Drudge Skeletons");
        assertThat(skele.isBlocking()).isFalse();
        assertThat(skele.getBlockingTargets()).isEmpty();
    }
}
