package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MosquitoGuard;
import com.github.laxika.magicalvibes.cards.m.MurmuringBosk;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoldwyrIntimidator.class, MosquitoGuard.class, MurmuringBosk.class})
class BoldwyrIntimidatorTest extends BaseCardTest {

    @Test
    @DisplayName("A Coward can't block a Warrior while Boldwyr Intimidator is on the battlefield")
    void cowardCannotBlockWarrior() {
        Permanent boldwyr = addCreatureReady(player1, new BoldwyrIntimidator());
        boldwyr.setAttacking(true); // Boldwyr is a Warrior
        Permanent blocker = addCreatureReady(player2, new MosquitoGuard());
        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cowards can't block Warriors");
    }

    @Test
    @DisplayName("A non-Coward creature can still block the Warrior Boldwyr")
    void nonCowardCanBlock() {
        Permanent boldwyr = addCreatureReady(player1, new BoldwyrIntimidator());
        boldwyr.setAttacking(true);
        addCreatureReady(player2, new MosquitoGuard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("A Coward can block a non-Warrior attacker")
    void cowardCanBlockNonWarrior() {
        addCreatureReady(player1, new BoldwyrIntimidator());
        Permanent attacker = addCreatureReady(player1, new MosquitoGuard());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MosquitoGuard());
        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("{R} makes a target creature a Coward, which then can't block the Warrior Boldwyr")
    void cowardAbilityStopsBlock() {
        Permanent boldwyr = addCreatureReady(player1, new BoldwyrIntimidator());
        Permanent blocker = addCreatureReady(player2, new MosquitoGuard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        boldwyr.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cowards can't block Warriors");
    }

    @Test
    @DisplayName("The Coward type wears off at end of turn, restoring the ability to block")
    void cowardWearsOff() {
        Permanent boldwyr = addCreatureReady(player1, new BoldwyrIntimidator());
        boldwyr.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MosquitoGuard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        blocker.resetModifiers(); // end-of-turn cleanup

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Becoming a Coward replaces the target creature's existing types")
    void cowardAbilityReplacesExistingTypes() {
        addCreatureReady(player1, new BoldwyrIntimidator());
        Permanent target = addCreatureReady(player1, new BoldwyrIntimidator());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        target.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MosquitoGuard());
        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("{2}{R} makes an attacker a Warrior, which a Coward then can't block")
    void warriorAbilityBlocksCoward() {
        // Boldwyr (index 0) supplies the static; the attacker (index 1) is a made-Warrior creature.
        addCreatureReady(player1, new BoldwyrIntimidator());
        Permanent attacker = addCreatureReady(player1, new MosquitoGuard());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MosquitoGuard());
        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cowards can't block Warriors");
    }

    @Test
    @DisplayName("The Warrior type wears off at end of turn")
    void warriorWearsOff() {
        addCreatureReady(player1, new BoldwyrIntimidator());
        Permanent target = addCreatureReady(player1, new MosquitoGuard());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.WARRIOR);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .containsExactlyInAnyOrder(CardSubtype.KITHKIN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("The type-changing abilities can only target creatures")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new BoldwyrIntimidator());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        harness.addMana(player1, ManaColor.RED, 3);
        UUID landId = land.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, landId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
