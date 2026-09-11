package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfDusk.class, GrizzlyBears.class})
class KnightOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack and resolves to battlefield")
    void castingAndResolving() {
        harness.castFromHand(player1, new KnightOfDusk(), "{1}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof KnightOfDusk);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack targeting blocking creature")
    void activatingPutsOnStack() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(KnightOfDusk.class);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
    }

    @Test
    @DisplayName("Resolving ability destroys the blocking creature")
    void resolvingDestroysBlocker() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Ability consumes {B}{B} mana")
    void manaIsConsumed() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent blocker = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent nonBlocker = addCreatureReady(player2, new GrizzlyBears());
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonBlocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature blocking this creature");
    }

    @Test
    @DisplayName("Cannot target creature blocking a different attacker")
    void cannotTargetCreatureBlockingDifferentAttacker() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        // Add a second attacker at index 1
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        otherAttacker.setAttacking(true);
        // Blocker is blocking the OTHER attacker (index 1), not the Knight (index 0)
        Permanent blocker = addBlocker(player2, 1);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature blocking this creature");
    }

    @Test
    @DisplayName("Can activate ability multiple times since it does not require tapping")
    void canActivateMultipleTimes() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent blocker1 = addBlocker(player2, 0);
        Permanent blocker2 = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 4);

        // Activate twice before resolving — both go on the stack
        harness.activateAbility(player1, 0, null, blocker1.getId());
        harness.activateAbility(player1, 0, null, blocker2.getId());

        // Knight should NOT be tapped (ability doesn't require tap)
        assertThat(knight.isTapped()).isFalse();

        assertThat(gd.stack).hasSize(2);

        // Resolve both abilities
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Ability fizzles if the blocker leaves combat before resolution")
    void fizzlesIfTargetStopsBlocking() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.clearCombatState();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Resolving ability adds destruction to game log")
    void resolvingAddsToGameLog() {
        Permanent knight = addCreatureReady(player1, new KnightOfDusk());
        knight.setAttacking(true);
        Permanent blocker = addBlocker(player2, 0);
        setupCombatStep();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("destroyed"));
    }

    private Permanent addBlocker(Player player, int attackerIndex) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
        return perm;
    }

    private void setupCombatStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}

