package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThornLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell targeting it creates an Elf Warrior token")
    void opponentSpellTargetingItCreatesToken() {
        Permanent lieutenant = addLieutenant(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's ability targeting it creates an Elf Warrior token")
    void opponentAbilityTargetingItCreatesToken() {
        Permanent lieutenant = addLieutenant(player1);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(1);
    }

    @Test
    @DisplayName("Its controller's spell does not create a token")
    void ownSpellDoesNotCreateToken() {
        Permanent lieutenant = addLieutenant(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("Paying {5}{G} gives it +4/+4 until end of turn")
    void pumpAbilityBoostsUntilEndOfTurn() {
        Permanent lieutenant = addLieutenant(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(3);
    }

    private Permanent addLieutenant(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThornLieutenant());
    }
}
