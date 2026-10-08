package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoldarenDuelist.class, DevilthornFox.class})
class VoldarenDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new VoldarenDuelist()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        // Resolve the creature spell and put its ETB ability on the stack.
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Target creature cannot declare as blocker after ETB resolves")
    void targetCannotDeclareAsBlocker() {
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());

        harness.setHand(player1, List.of(new VoldarenDuelist()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = blocker.getId();
        harness.castCreature(player1, 0, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering an empty battlefield still requires a target for the ETB")
    void canCastWithoutTargetWhenNoCreatures() {
        harness.setHand(player1, List.of(new VoldarenDuelist()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voldaren Duelist");
        Permanent duelist = findPermanent(player1, "Voldaren Duelist");
        harness.handlePermanentChosen(player1, duelist.getId());
        harness.passBothPriorities();
        assertThat(duelist.isCantBlockThisTurn()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new VoldarenDuelist()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Devilthorn Fox");
        harness.castCreature(player1, 0, 0, targetId);

        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can target a friendly creature without affecting other creatures")
    void canTargetFriendlyCreature() {
        Permanent target = addCreatureReady(player1, new DevilthornFox());
        Permanent other = addCreatureReady(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new VoldarenDuelist()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        assertThat(findPermanent(player1, "Voldaren Duelist").isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Blocking restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new VoldarenDuelist()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Haste allows Duelist to attack on the turn it enters")
    void canAttackOnTurnItEnters() {
        Permanent target = addCreatureReady(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new VoldarenDuelist()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(findPermanent(player1, "Voldaren Duelist").isAttacking()).isTrue();
    }
}
