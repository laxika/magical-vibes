package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnworthyDead.class})
class UnworthyDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Unworthy Dead's regeneration ability creates a regeneration shield")
    void activationCreatesRegenerationShield() {
        Permanent dead = addCreatureReady(player1, new UnworthyDead());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(dead.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The regeneration ability cannot be activated without black mana")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new UnworthyDead());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration shield saves Unworthy Dead from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent dead = addCreatureReady(player1, new UnworthyDead());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        dead.setBlocking(true);
        dead.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new UnworthyDead());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Unworthy Dead");
        Permanent survivingDead = findPermanent(player1, "Unworthy Dead");
        assertThat(survivingDead.isTapped()).isTrue();
        assertThat(survivingDead.getRegenerationShield()).isZero();
    }
}
