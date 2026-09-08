package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuneTailKitsuneAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Flips when its controller has 30 or more life")
    void flipsAtThirtyLife() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        harness.setLife(player1, 30);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(runeTail.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip below 30 life")
    void doesNotFlipBelowThirtyLife() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        harness.setLife(player1, 29);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(runeTail.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Rune-Tail's Essence prevents damage to creatures its controller controls")
    void essencePreventsDamageToControlledCreatures() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        runeTail.setTransformed(true);
        runeTail.setCard(runeTail.getOriginalCard().getBackFaceCard());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
    }
}
