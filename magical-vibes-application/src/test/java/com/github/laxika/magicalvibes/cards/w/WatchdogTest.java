package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WatchdogTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures attacking its controller get -1/-0 while it is untapped")
    void shrinksAttackersWhileUntapped() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        beginCombat(attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-0 goes away while it is tapped")
    void noDebuffWhileTapped() {
        Permanent watchdog = addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        watchdog.tap();
        beginCombat(attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures attacking a different player are unaffected")
    void doesNotShrinkCreaturesAttackingSomeoneElse() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declaring no blockers is illegal — it blocks each combat if able")
    void mustBlockEachCombat() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Blocking satisfies the requirement")
    void blockingSatisfiesRequirement() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No requirement when it can't legally block (tapped)")
    void noRequirementWhenTapped() {
        Permanent watchdog = addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        watchdog.tap();
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
