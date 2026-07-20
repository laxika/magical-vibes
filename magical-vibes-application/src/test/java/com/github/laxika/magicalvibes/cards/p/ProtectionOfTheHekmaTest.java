package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class ProtectionOfTheHekmaTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from an opponent's spell dealing damage to controller")
    void prevents1DamageFromOpponentSpell() {
        harness.setLife(player1, 20);
        addProtectionOfTheHekma(player1);

        // Opponent casts Shock (2 damage) targeting player1
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // 2 damage - 1 prevented = 1 damage taken
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent damage from controller's own sources")
    void doesNotPreventDamageFromOwnSources() {
        harness.setLife(player1, 20);
        addProtectionOfTheHekma(player1);

        // Player1 casts Shock targeting self
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Full 2 damage — no prevention for own sources
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 1 from each opponent source in combat — per attacker")
    void prevents1PerAttackerInCombat() {
        harness.setLife(player2, 20);
        addProtectionOfTheHekma(player2);

        // Player1 attacks with two 2/2 creatures
        addReadyAttacker(player1, new GrizzlyBears());
        addReadyAttacker(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Two 2/2 attackers: each deals 2 - 1 = 1 damage, total = 2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent damage dealt to an opponent")
    void doesNotPreventDamageDealtToOpponent() {
        harness.setLife(player2, 20);
        addProtectionOfTheHekma(player1);

        // Player1 casts Shock targeting opponent
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Opponent takes full 2 damage — only protects its controller
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void addProtectionOfTheHekma(Player player) {
        Permanent perm = new Permanent(new ProtectionOfTheHekma());
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void addReadyAttacker(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
