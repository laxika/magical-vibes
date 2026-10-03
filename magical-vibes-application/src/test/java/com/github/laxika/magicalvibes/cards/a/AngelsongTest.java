package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Angelsong.class, CylianElf.class, Skullcrack.class})
class AngelsongTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage this turn")
    void preventsAllCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Angelsong()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Resolve Angelsong — sets the prevent-all-combat-damage shield for the turn.
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.stack).isEmpty();

        addUnblockedAttacker(player1); // Cylian Elf 2/2
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance through combat damage — attacker is unblocked but all damage is prevented.
        harness.getGameService().passPriority(gd, player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Angelsong()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Angelsong");
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    void preventsDamageToAttackerAndBlocker() {
        harness.setHand(player1, List.of(new Angelsong()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        Permanent attacker = addUnblockedAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new CylianElf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Cylian Elf");
        harness.assertOnBattlefield(player2, "Cylian Elf");
    }

    @Test
    void cyclingPaysDiscardBeforeDrawingAndDoesNotPreventCombatDamage() {
        harness.setHand(player1, List.of(new Angelsong()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Angelsong");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Cylian Elf");

        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotCycleWithoutTwoMana() {
        harness.setHand(player1, List.of(new Angelsong()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Angelsong");
        harness.assertNotInGraveyard(player1, "Angelsong");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preventionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new Angelsong()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addUnblockedAttacker(player2);
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
    }

    @Test
    void cannotPreventCombatDamageAfterSkullcrack() {
        harness.setHand(player1, List.of(new Angelsong(), new Skullcrack()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);
        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 15);
    }

    private Permanent addUnblockedAttacker(Player player) {
        Permanent perm = addCreatureReady(player, new CylianElf());
        perm.setAttacking(true);
        return perm;
    }
}
