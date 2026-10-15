package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.g.GhituFire;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallousGiant.class, GhituFire.class, AncientKavu.class, Humble.class})
class CallousGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents a damage event of 3 or less")
    void preventsDamageAtOrBelowThreshold() {
        Permanent giant = addCreatureReady(player2, new CallousGiant());
        UUID giantId = giant.getId();
        harness.setHand(player1, List.of(new GhituFire(), new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 2, giantId);
        harness.castAndResolveSorcery(player1, 0, 3, giantId);

        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Callous Giant");
    }

    @Test
    @DisplayName("Does not prevent a damage event above 3")
    void doesNotPreventDamageAboveThreshold() {
        Permanent giant = addCreatureReady(player2, new CallousGiant());
        UUID giantId = giant.getId();
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 4, giantId);

        harness.assertNotOnBattlefield(player2, "Callous Giant");
        harness.assertInGraveyard(player2, "Callous Giant");
    }

    @Test
    @DisplayName("Only prevents damage to itself")
    void onlyPreventsDamageToItself() {
        Permanent giant = addCreatureReady(player2, new CallousGiant());
        Permanent otherCreature = addCreatureReady(player2, new AncientKavu());
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, otherCreature.getId());

        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents combat damage of 3 or less")
    void preventsCombatDamageAtOrBelowThreshold() {
        Permanent giant = addCreatureReady(player1, new CallousGiant());
        giant.setBlocking(true);
        giant.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AncientKavu());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Callous Giant");
    }

    @Test
    @DisplayName("Prevents simultaneous small combat damage from each source separately")
    void preventsDamageFromMultipleBlockersSeparately() {
        Permanent giant = addCreatureReady(player1, new CallousGiant());
        giant.setAttacking(true);
        for (int i = 0; i < 2; i++) {
            Permanent blocker = addCreatureReady(player2, new AncientKavu());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
        }

        resolveCombat(player1);
        List<Permanent> blockers = findPermanents(player2, "Ancient Kavu");
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blockers.get(0).getId(), 2,
                blockers.get(1).getId(), 2));

        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Callous Giant");
    }

    @Test
    @DisplayName("Does not prevent combat damage above the threshold")
    void doesNotPreventFourCombatDamage() {
        Permanent giant = addCreatureReady(player1, new CallousGiant());
        giant.setBlocking(true);
        giant.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new CallousGiant());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Callous Giant");
        harness.assertInGraveyard(player2, "Callous Giant");
    }

    @Test
    @DisplayName("Losing all abilities removes its damage prevention")
    void doesNotPreventDamageAfterLosingAbilities() {
        Permanent giant = addCreatureReady(player2, new CallousGiant());
        harness.setHand(player1, List.of(new Humble(), new GhituFire()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, giant.getId());
        harness.castAndResolveSorcery(player1, 0, 1, giant.getId());

        harness.assertNotOnBattlefield(player2, "Callous Giant");
        harness.assertInGraveyard(player2, "Callous Giant");
    }
}
