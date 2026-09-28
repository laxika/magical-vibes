package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.g.GhituFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallousGiant.class, GhituFire.class, AncientKavu.class})
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
}
