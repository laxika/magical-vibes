package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManorSkeletonTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Manor Skeleton has regenerate activated ability with cost {1}{B}")
    void hasCorrectAbility() {
        ManorSkeleton card = new ManorSkeleton();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{B}");
    }

    // ===== Casting =====

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

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Manor Skeleton"));
    }

    // ===== Haste — can attack immediately =====

    @Test
    @DisplayName("Manor Skeleton can attack the turn it enters the battlefield due to Haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent skeleton = new Permanent(new ManorSkeleton());
        skeleton.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(skeleton);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Regeneration ability =====

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

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Manor Skeleton from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent skelePerm = addManorSkeletonReady(player1);
        skelePerm.setRegenerationShield(1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Manor Skeleton"));
        Permanent skele = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Manor Skeleton"))
                .findFirst().orElseThrow();
        assertThat(skele.isTapped()).isTrue();
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Manor Skeleton dies without regeneration shield in combat")
    void diesWithoutRegenerationShieldInCombat() {
        Permanent skelePerm = addManorSkeletonReady(player1);
        skelePerm.setBlocking(true);
        skelePerm.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Manor Skeleton"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Manor Skeleton"));
    }

    // ===== Helper methods =====

    private Permanent addManorSkeletonReady(Player player) {
        ManorSkeleton card = new ManorSkeleton();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
