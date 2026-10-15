package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManorSkeleton.class, DarkthicketWolf.class, VictimOfNight.class, DeadWeight.class})
class ManorSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Manor Skeleton puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ManorSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Manor Skeleton");
    }

    @Test
    @DisplayName("Resolving Manor Skeleton puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new ManorSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Manor Skeleton");
    }

    @Test
    @DisplayName("Manor Skeleton can attack the turn it enters the battlefield due to Haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        skeleton.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent skelePerm = addManorSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Manor Skeleton");
        assertThat(entry.getTargetId()).isEqualTo(skelePerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addManorSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent skele = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(skele.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability (costs {1}{B})")
    void manaIsConsumedWhenActivating() {
        addManorSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addManorSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1); // needs {1}{B} = 2 mana

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Manor Skeleton from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent skelePerm = addManorSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Manor Skeleton");
        Permanent skele = findPermanent(player1, "Manor Skeleton");
        assertThat(skele.isTapped()).isTrue();
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
        assertThat(skele.getMarkedDamage()).isZero();
        assertThat(skele.isBlocking()).isFalse();
        assertThat(skele.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Manor Skeleton dies without regeneration shield in combat")
    void diesWithoutRegenerationShieldInCombat() {
        Permanent skelePerm = addManorSkeletonReady(player1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new DarkthicketWolf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Manor Skeleton");
        harness.assertInGraveyard(player1, "Manor Skeleton");
    }

    @Test
    @DisplayName("Creating a regeneration shield does not tap a summoning-sick creature")
    void shieldDoesNotTapCreature() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        skeleton.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skeleton.isTapped()).isFalse();
        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield replaces only one destruction")
    void shieldProtectsOnlyOnce() {
        Permanent skeleton = addManorSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new VictimOfNight(), new VictimOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, skeleton.getId());

        harness.assertOnBattlefield(player1, "Manor Skeleton");
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(skeleton.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player2, 0, skeleton.getId());

        harness.assertNotOnBattlefield(player1, "Manor Skeleton");
        harness.assertInGraveyard(player1, "Manor Skeleton");
    }

    @Test
    @DisplayName("Regeneration cannot save a creature with zero or less toughness")
    void regenerationDoesNotPreventToughnessDeath() {
        Permanent skeleton = addManorSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new DeadWeight()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castEnchantment(player2, 0, skeleton.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Manor Skeleton");
        harness.assertInGraveyard(player1, "Manor Skeleton");
    }

    private Permanent addManorSkeletonReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ManorSkeleton());
        perm.setSummoningSick(false);
        return perm;
    }
}
