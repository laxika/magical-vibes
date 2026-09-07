package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroningBureaucrats.class, GrizzlyBears.class, LlanowarElves.class})
class DroningBureaucratsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with mana value X can't attack")
    void matchingCreaturesCannotAttack() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());

        activate(bureaucrats, 2);

        assertThatThrownBy(() -> declareAttack(attackingBears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures with mana value X can't block")
    void matchingCreaturesCannotBlock() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent attacker = addCreatureReady(player1, new LlanowarElves());
        Permanent blockingBears = addCreatureReady(player2, new GrizzlyBears());

        activate(bureaucrats, 2);

        assertThatThrownBy(() -> declareBlock(attacker, blockingBears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Creatures with a different mana value are unaffected")
    void differentManaValueIsUnaffected() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());

        activate(bureaucrats, 2);

        assertThatCode(() -> declareAttack(elf)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent bureaucrats = addCreatureReady(player1, new DroningBureaucrats());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        activate(bureaucrats, 2);
        gd.expireEndOfTurnFloatingEffects();

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    private void activate(Permanent bureaucrats, int xValue) {
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bureaucrats), xValue, null);
        harness.passBothPriorities();
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
