package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CallousGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents a damage event of 3 or less")
    void preventsDamageAtOrBelowThreshold() {
        Permanent giant = addCreatureReady(player2, new CallousGiant());
        UUID giantId = giant.getId();
        harness.setHand(player1, List.of(new Shock(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Callous Giant");
    }

    @Test
    @DisplayName("Does not prevent a damage event above 3")
    void doesNotPreventDamageAboveThreshold() {
        Permanent giant = addCreatureReady(player2, new CallousGiant());
        UUID giantId = giant.getId();
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 4, List.of(giantId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Callous Giant");
        harness.assertInGraveyard(player2, "Callous Giant");
    }

    @Test
    @DisplayName("Prevents combat damage of 3 or less")
    void preventsCombatDamageAtOrBelowThreshold() {
        Permanent giant = addCreatureReady(player1, new CallousGiant());
        giant.setBlocking(true);
        giant.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Callous Giant");
    }
}
