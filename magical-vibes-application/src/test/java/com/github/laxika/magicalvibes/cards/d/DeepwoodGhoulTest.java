package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeepwoodGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life grants a regeneration shield")
    void payLifeGrantsRegenerationShield() {
        Permanent ghoul = addCreatureReady(player1, new DeepwoodGhoul());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(ghoul.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability with less than 2 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new DeepwoodGhoul());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Regeneration shield saves Deepwood Ghoul from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Deepwood Ghoul (2/1) with a regen shield blocks Grizzly Bears (2/2)
        Permanent ghoul = addCreatureReady(player1, new DeepwoodGhoul());
        ghoul.setRegenerationShield(1);
        ghoul.setBlocking(true);
        ghoul.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Ghoul survives via regeneration, tapped, shield consumed
        harness.assertOnBattlefield(player1, "Deepwood Ghoul");
        assertThat(ghoul.isTapped()).isTrue();
        assertThat(ghoul.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deepwood Ghoul dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent ghoul = addCreatureReady(player1, new DeepwoodGhoul());
        ghoul.setBlocking(true);
        ghoul.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deepwood Ghoul");
        harness.assertInGraveyard(player1, "Deepwood Ghoul");
    }
}
