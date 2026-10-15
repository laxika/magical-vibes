package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
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

@CardUsed({ProtectionOfTheHekma.class, Shock.class, GrizzlyBears.class, ProdigalPyromancer.class, RayOfCommand.class})
class ProtectionOfTheHekmaTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from an opponent's spell dealing damage to controller")
    void prevents1DamageFromOpponentSpell() {
        harness.setLife(player1, 20);
        addProtectionOfTheHekma(player1);

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
        addProtectionOfTheHekma(player1);

        // Player1 casts Shock targeting self
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

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
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Opponent takes full 2 damage — only protects its controller
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void multipleCopiesPreventDamageCumulatively() {
        addProtectionOfTheHekma(player1);
        addProtectionOfTheHekma(player1);
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    void preventsDamageAgainOnEachSeparateEvent() {
        addProtectionOfTheHekma(player1);
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotProtectControllersCreatures() {
        addProtectionOfTheHekma(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void preventsAllOfOneDamageFromAnOpponentAbility() {
        addProtectionOfTheHekma(player1);
        harness.setLife(player1, 20);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotPreventDamageAfterTakingControlOfTheAbilitySource() {
        addProtectionOfTheHekma(player1);
        harness.setLife(player1, 20);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.castAndResolveInstant(player1, 0, pyromancer.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pyromancer);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void preventsDamageAfterOpponentTakesControlOfTheAbilitySource() {
        addProtectionOfTheHekma(player1);
        harness.setLife(player1, 20);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.castAndResolveInstant(player2, 0, pyromancer.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pyromancer);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    private void addProtectionOfTheHekma(Player player) {
        harness.addToBattlefield(player, new ProtectionOfTheHekma());
    }

    private void addReadyAttacker(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
    }
}
