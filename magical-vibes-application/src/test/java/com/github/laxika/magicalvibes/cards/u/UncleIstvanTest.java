package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BallLightning;
import com.github.laxika.magicalvibes.cards.b.BrothersOfFire;
import com.github.laxika.magicalvibes.cards.i.Inferno;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UncleIstvan.class, BallLightning.class, BrothersOfFire.class, Inferno.class})
class UncleIstvanTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature is prevented")
    void combatDamageFromCreatureIsPrevented() {
        Permanent istvan = addCreatureReady(player1, new UncleIstvan());
        istvan.setBlocking(true);
        istvan.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new BallLightning());
        attacker.setAttacking(true);

        resolveCombat(player2);

        // Ball Lightning (6/1) would be lethal to a 1/3, but all combat damage from the creature is prevented.
        harness.assertOnBattlefield(player1, "Uncle Istvan");
        assertThat(istvan.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage from a creature source is prevented")
    void noncombatCreatureSourceDamageIsPrevented() {
        Permanent istvan = addCreatureReady(player2, new UncleIstvan());
        addCreatureReady(player1, new BrothersOfFire());
        UUID istvanId = harness.getPermanentId(player2, "Uncle Istvan");

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, istvanId);
        harness.passBothPriorities();

        assertThat(istvan.getMarkedDamage()).isZero();
        harness.assertLife(player1, 19);
        harness.assertOnBattlefield(player2, "Uncle Istvan");
    }

    @Test
    @DisplayName("Damage from a noncreature source (a spell) is not prevented")
    void spellSourceDamageIsNotPrevented() {
        addCreatureReady(player2, new UncleIstvan());
        harness.setHand(player1, List.of(new Inferno()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Inferno is an instant (a noncreature source), so its damage is not prevented.
        harness.assertNotOnBattlefield(player2, "Uncle Istvan");
        harness.assertInGraveyard(player2, "Uncle Istvan");
    }

    @Test
    @DisplayName("Prevents noncombat creature damage after the source leaves the battlefield")
    void noncombatCreatureSourceDamageIsPreventedAfterSourceLeavesBattlefield() {
        Permanent istvan = addCreatureReady(player2, new UncleIstvan());
        Permanent brothers = addCreatureReady(player1, new BrothersOfFire());
        UUID istvanId = istvan.getId();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, istvanId);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, brothers);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(brothers);
        harness.passBothPriorities();

        assertThat(istvan.getMarkedDamage()).isZero();
        harness.assertLife(player1, 19);
        harness.assertOnBattlefield(player2, "Uncle Istvan");
    }

    @Test
    @CardUsed(TurnToFrog.class)
    @DisplayName("Does not prevent creature damage after losing its ability")
    void doesNotPreventDamageAfterLosingAbility() {
        addCreatureReady(player2, new UncleIstvan());
        Permanent brothers = addCreatureReady(player1, new BrothersOfFire());
        UUID istvanId = harness.getPermanentId(player2, "Uncle Istvan");

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, istvanId);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, istvanId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brothers);
        harness.assertInGraveyard(player2, "Uncle Istvan");
    }
}
