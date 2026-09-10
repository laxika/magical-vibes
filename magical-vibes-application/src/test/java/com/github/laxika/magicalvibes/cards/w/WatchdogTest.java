package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Watchdog.class, HornedTurtle.class, WindDrake.class})
class WatchdogTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures attacking its controller get -1/-0 while it is untapped")
    void shrinksAttackersWhileUntapped() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());

        beginCombat(attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Every creature attacking its controller gets -1/-0")
    void shrinksEveryAttacker() {
        addCreatureReady(player2, new Watchdog());
        Permanent firstAttacker = addCreatureReady(player1, new HornedTurtle());
        Permanent secondAttacker = addCreatureReady(player1, new HornedTurtle());

        firstAttacker.setAttacking(true);
        firstAttacker.setAttackTarget(player2.getId());
        secondAttacker.setAttacking(true);
        secondAttacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(0);
    }

    @Test
    @DisplayName("The -1/-0 goes away while it is tapped")
    void noDebuffWhileTapped() {
        Permanent watchdog = addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());

        watchdog.tap();
        beginCombat(attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures attacking a different player are unaffected")
    void doesNotShrinkCreaturesAttackingSomeoneElse() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declaring no blockers is illegal — it blocks each combat if able")
    void mustBlockEachCombat() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Blocking satisfies the requirement")
    void blockingSatisfiesRequirement() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No requirement when it can't legally block (tapped)")
    void noRequirementWhenTapped() {
        Permanent watchdog = addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new HornedTurtle());

        watchdog.tap();
        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No requirement when no attacker can be blocked")
    void noRequirementWhenAttackerHasFlying() {
        addCreatureReady(player2, new Watchdog());
        Permanent attacker = addCreatureReady(player1, new WindDrake());

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();
    }
}
