package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Brushstrider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzoriusJusticiar.class, Brushstrider.class, AxebaneGuardian.class})
class AzoriusJusticiarTest extends BaseCardTest {

    @Test
    @DisplayName("Both detained creatures can't attack")
    void bothDetainedCreaturesCannotAttack() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new Brushstrider());

        castJusticiar(List.of(bear1.getId(), bear2.getId()));

        assertThatThrownBy(() -> declareAttack(bear1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThatThrownBy(() -> declareAttack(bear2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities")
    void detainedCreatureCannotActivateAbilities() {
        Permanent guardian = addCreatureReady(player2, new AxebaneGuardian());

        castJusticiar(List.of(guardian.getId()));

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Can enter with zero targets (up to two)")
    void canEnterWithNoTargets() {
        castJusticiar(List.of());

        harness.assertOnBattlefield(player1, "Azorius Justiciar");
    }

    @Test
    @DisplayName("Detain wears off at the Justiciar controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        castJusticiar(List.of(bear.getId()));

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot detain a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new Brushstrider());
        harness.setHand(player1, List.of(new AzoriusJusticiar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Detained creatures cannot block")
    void detainedCreaturesCannotBlock() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        castJusticiar(List.of(first.getId(), second.getId()));
        Permanent attacker = addCreatureReady(player1, new Brushstrider());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(1, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("An opponent's turn does not end detain")
    void detainPersistsDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        castJusticiar(List.of(creature.getId()));

        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures not targeted by detain can still attack")
    void untargetedCreatureCanAttack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        castJusticiar(List.of(target.getId()));

        assertThatCode(() -> declareAttack(other)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Detain resolves on the remaining target if one target leaves")
    void remainingTargetIsDetained() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        castJusticiarSpell(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());

        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(second))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A target that changes to your control is not detained")
    void targetChangingControllerIsNotDetained() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        castJusticiarSpell(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerBattlefields.get(player1.getId()).add(first);

        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Brushstrider());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(first);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatCode(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Detain still resolves after Justiciar leaves")
    void detainDoesNotDependOnSourceRemaining() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Brushstrider());
        castJusticiarSpell(List.of(creature.getId()));
        harness.passBothPriorities();
        Permanent justiciar = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(justiciar);
        gd.playerGraveyards.get(player1.getId()).add(justiciar.getCard());

        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    /** Casts the Justiciar on the given targets and resolves both the spell and its ETB trigger. */
    private void castJusticiar(List<UUID> targetIds) {
        castJusticiarSpell(targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castJusticiarSpell(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new AzoriusJusticiar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, targetIds);
    }

    /** Attempts to declare the given player2 creature as an attacker. */
    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, List.of(index));
    }
}
