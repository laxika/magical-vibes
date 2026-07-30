package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PredatoryRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control +3/+3 and leaves opponent's creatures alone")
    void boostsOwnCreaturesOnly() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        castRampage();

        assertThat(own.getEffectivePower()).isEqualTo(5);
        assertThat(own.getEffectiveToughness()).isEqualTo(5);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creatures must block if able")
    void opponentCreaturesMustBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castRampage();

        assertThat(blocker.isMustBlockThisTurnIfAble()).isTrue();

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Declaring the forced block is legal")
    void forcedBlockCanBeSatisfied() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castRampage();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not force the caster's own creatures to block")
    void doesNotForceOwnCreatures() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        castRampage();

        assertThat(own.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Boost and block requirement wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        castRampage();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Resolves with no creatures on the battlefield")
    void resolvesWithEmptyBattlefield() {
        castRampage();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Predatory Rampage");
    }

    private void castRampage() {
        harness.setHand(player1, List.of(new PredatoryRampage()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
