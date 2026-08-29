package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BelligerentSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazorgrassScreenTest extends BaseCardTest {

    @Test
    @DisplayName("It must block each combat if able")
    void mustBlockEachCombat() {
        addCreatureReady(player2, new RazorgrassScreen());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Blocking satisfies the requirement")
    void blockingSatisfiesRequirement() {
        addCreatureReady(player2, new RazorgrassScreen());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A lone Screen is not able to block a menace attacker")
    void menaceMakesBlockingRequirementImpossible() {
        addCreatureReady(player2, new RazorgrassScreen());
        Permanent attacker = addCreatureReady(player1, new BelligerentSliver());

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Enough blockers still have to block a menace attacker")
    void enoughBlockersMustBlockMenaceAttacker() {
        addCreatureReady(player2, new RazorgrassScreen());
        addCreatureReady(player2, new RazorgrassScreen());
        Permanent attacker = addCreatureReady(player1, new BelligerentSliver());

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A tapped Razorgrass Screen is not required to block")
    void noRequirementWhenTapped() {
        Permanent screen = addCreatureReady(player2, new RazorgrassScreen());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        screen.tap();
        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
