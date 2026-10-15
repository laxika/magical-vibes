package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({SpinEngine.class})
class SpinEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an opponent's creature")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new SpinEngine());
        Permanent target = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent target = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(engine.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new SpinEngine());
        Permanent target = addCreatureReady(player2, new SpinEngine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving ability adds source to target's cantBlockIds")
    void resolvingAbilityAddsCantBlockRestriction() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent target = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCantBlockIds()).contains(engine.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addCreatureReady(player1, new SpinEngine());
        Permanent target = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        // Should resolve without error (fizzle)
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCantBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Targeted creature cannot block Spin Engine after ability resolves")
    void targetedCreatureCannotBlockSpinEngine() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent blocker = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: Spin Engine attacks
        engine.setAttacking(true);
        prepareDeclareBlockers(player1);

        // Attempting to block Spin Engine with the targeted creature should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Targeted creature can still block other creatures")
    void targetedCreatureCanBlockOtherCreatures() {
        addCreatureReady(player1, new SpinEngine());
        Permanent otherAttacker = addCreatureReady(player1, new SpinEngine());
        Permanent blocker = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability targeting the blocker
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: only the other Spin Engine attacks
        otherAttacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        // The restriction applies only to the source permanent.
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Non-targeted creature can still block Spin Engine")
    void nonTargetedCreatureCanBlockSpinEngine() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent targetedBlocker = addCreatureReady(player2, new SpinEngine());
        addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability targeting only the first blocker
        harness.activateAbility(player1, 0, null, targetedBlocker.getId());
        harness.passBothPriorities();

        // Set up combat: Spin Engine attacks
        engine.setAttacking(true);
        prepareDeclareBlockers(player1);

        // The second blocker is unrestricted.
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
    }

    @Test
    @DisplayName("Can activate ability multiple times on different creatures")
    void canActivateMultipleTimes() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent blocker1 = addCreatureReady(player2, new SpinEngine());
        Permanent blocker2 = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 2);

        // Activate on first blocker and resolve
        harness.activateAbility(player1, 0, null, blocker1.getId());
        harness.passBothPriorities();

        // Activate on second blocker and resolve
        harness.activateAbility(player1, 0, null, blocker2.getId());
        harness.passBothPriorities();

        assertThat(blocker1.getCantBlockIds()).contains(engine.getId());
        assertThat(blocker2.getCantBlockIds()).contains(engine.getId());
    }

    @Test
    @DisplayName("Blocking restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent blocker = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(engine.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(blocker.getCantBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Spin Engine can activate its ability")
    void tappedSummoningSickSourceCanActivate() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new SpinEngine());
        engine.tap();
        Permanent target = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCantBlockIds()).contains(engine.getId());
        assertThat(engine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target a creature its controller controls")
    void canTargetOwnCreature() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent target = addCreatureReady(player1, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCantBlockIds()).contains(engine.getId());
    }

    @Test
    @DisplayName("The ability can target Spin Engine itself")
    void canTargetItself() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, engine.getId());
        harness.passBothPriorities();

        assertThat(engine.getCantBlockIds()).contains(engine.getId());
    }

    @Test
    @DisplayName("The ability resolves even if Spin Engine leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent engine = addCreatureReady(player1, new SpinEngine());
        Permanent target = addCreatureReady(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(engine);
        gd.playerGraveyards.get(player1.getId()).add(engine.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCantBlockIds()).contains(engine.getId());
    }

    @Test
    @DisplayName("A player is not a legal target for the ability")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new SpinEngine());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

}
