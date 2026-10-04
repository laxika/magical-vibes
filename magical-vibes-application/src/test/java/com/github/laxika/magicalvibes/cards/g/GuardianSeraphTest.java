package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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

@CardUsed({GuardianSeraph.class, Shock.class, GrizzlyBears.class, TurnToFrog.class})
class GuardianSeraphTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from opponent's spell dealing damage to controller")
    void prevents1DamageFromOpponentSpell() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GuardianSeraph());

        // Opponent casts Shock (2 damage) targeting player1
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        // 2 damage - 1 prevented = 1 damage taken
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not prevent damage from controller's own sources")
    void doesNotPreventDamageFromOwnSources() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GuardianSeraph());

        // Player1 casts Shock targeting self
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        // Full 2 damage — no prevention for own sources
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two Guardian Seraphs prevent 2 damage total")
    void twoGuardianSeraphsPrevent2Damage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GuardianSeraph());
        harness.addToBattlefield(player1, new GuardianSeraph());

        // Opponent casts Shock (2 damage) targeting player1
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        // 2 damage - 2 prevented = 0 damage taken
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents 1 from each opponent source in combat — per attacker")
    void prevents1PerAttackerInCombat() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GuardianSeraph());

        // Player1 attacks with two 2/2 creatures
        addReadyAttacker(player1, new GrizzlyBears());
        addReadyAttacker(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Two 2/2 bears attack: each deals 2 - 1 = 1 damage, total = 2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent damage dealt to opponent")
    void doesNotPreventDamageDealtToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GuardianSeraph());

        // Player1 casts Shock targeting opponent
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Opponent takes full 2 damage — Guardian Seraph only protects its controller
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevention does not reduce damage below 0")
    void doesNotReduceBelowZero() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GuardianSeraph());
        harness.addToBattlefield(player1, new GuardianSeraph());
        harness.addToBattlefield(player1, new GuardianSeraph());

        // Opponent casts Shock (2 damage) targeting player1
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        // 2 damage - 3 prevented = 0 (clamped at 0)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevention applies again to later damage events")
    void preventsDamageFromEachSpellInTheSameTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GuardianSeraph());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Guardian Seraph does not prevent damage to creatures")
    void doesNotPreventDamageToCreatures() {
        harness.addToBattlefield(player1, new GuardianSeraph());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevention stops when Guardian Seraph leaves the battlefield")
    void stopsPreventingAfterLeavingBattlefield() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new GuardianSeraph());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        java.util.UUID seraphId = harness.getPermanentId(player1, "Guardian Seraph");

        harness.castAndResolveInstant(player2, 0, seraphId);
        harness.castAndResolveInstant(player2, 0, seraphId);
        harness.assertInGraveyard(player1, "Guardian Seraph");
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Guardian Seraph cannot prevent damage after losing all abilities")
    void stopsPreventingAfterLosingAbilities() {
        harness.setLife(player1, 20);
        Permanent seraph = harness.addToBattlefieldAndReturn(player1, new GuardianSeraph());
        harness.setHand(player2, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, seraph.getId());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    private Permanent addReadyAttacker(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        return perm;
    }
}
