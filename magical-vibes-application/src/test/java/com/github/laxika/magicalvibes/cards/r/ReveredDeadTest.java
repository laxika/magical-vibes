package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReveredDead.class, GrizzlyBears.class})
class ReveredDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Paying white grants a regeneration shield")
    void whiteActivationGrantsRegenerationShield() {
        Permanent dead = addCreatureReady(player1, new ReveredDead());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Revered Dead from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent dead = addCreatureReady(player1, new ReveredDead());
        dead.setRegenerationShield(1);
        dead.setBlocking(true);
        dead.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Revered Dead");
        assertThat(dead.isTapped()).isTrue();
        assertThat(dead.getRegenerationShield()).isZero();
    }
}
