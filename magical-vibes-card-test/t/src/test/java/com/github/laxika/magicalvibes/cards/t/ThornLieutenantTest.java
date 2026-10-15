package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornLieutenant.class, Shock.class, ProdigalPyromancer.class, TurnToFrog.class})
class ThornLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell targeting it creates an Elf Warrior token")
    void opponentSpellTargetingItCreatesToken() {
        Permanent lieutenant = addLieutenant(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, lieutenant.getId());

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

        harness.castAndResolveInstant(player1, 0, lieutenant.getId());

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

    @Test
    @DisplayName("Losing all abilities prevents the token trigger from an opponent's spell")
    void lostAbilitiesPreventSpellTrigger() {
        Permanent lieutenant = addLieutenant(player1);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, lieutenant.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, lieutenant.getId());

        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("Losing all abilities prevents the token trigger from an opponent's ability")
    void lostAbilitiesPreventAbilityTrigger() {
        Permanent lieutenant = addLieutenant(player1);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, lieutenant.getId());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("Its controller's targeted ability does not create a token")
    void ownAbilityDoesNotCreateToken() {
        Permanent lieutenant = addLieutenant(player1);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, lieutenant.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("The pump can be activated while summoning sick and multiple boosts accumulate")
    void repeatedPumpActivationsAccumulate() {
        Permanent lieutenant = addLieutenant(player1);
        lieutenant.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lieutenant)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, lieutenant)).isEqualTo(11);
        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }
    private Permanent addLieutenant(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThornLieutenant());
    }
}
