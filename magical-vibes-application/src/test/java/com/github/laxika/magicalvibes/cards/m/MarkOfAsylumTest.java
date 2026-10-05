package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkOfAsylum.class, GrizzlyBears.class, Shock.class, Banefire.class, Naturalize.class, VolcanicFallout.class})
class MarkOfAsylumTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage to a creature you control is prevented")
    void preventsNoncombatDamageToYourCreature() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        // Shock is noncombat damage; Mark of Asylum prevents it entirely.
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent's creature is not prevented")
    void doesNotPreventDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, enemyBears.getId());

        // Mark of Asylum only protects the controller's own creatures.
        assertThat(enemyBears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a creature you control is not prevented")
    void doesNotPreventCombatDamage() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Combat damage is unaffected; the blocker takes the attacker's 2 damage.
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void preventsDamageFromYourOwnSpell() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void massDamageProtectsOnlyYourCreaturesAndDoesNotProtectPlayers() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new VolcanicFallout()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0);

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void damageThatCannotBePreventedStillKillsProtectedCreature() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Banefire()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player2, 0, 5, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void preventableBanefireDamageIsPreventedBelowThreshold() {
        harness.addToBattlefield(player1, new MarkOfAsylum());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Banefire()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player2, 0, 4, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void protectionEndsWhenEnchantmentLeavesBeforeDamageResolves() {
        Permanent mark = harness.addToBattlefieldAndReturn(player1, new MarkOfAsylum());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mark.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mark of Asylum");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
