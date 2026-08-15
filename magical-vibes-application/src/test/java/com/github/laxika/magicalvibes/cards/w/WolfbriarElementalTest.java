package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WolfbriarElementalTest extends BaseCardTest {

    @Test
    void createsNoWolvesWithoutMultikicker() {
        harness.setHand(player1, List.of(new WolfbriarElemental()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void createsOneWolfPerMultikickerPayment() {
        harness.setHand(player1, List.of(new WolfbriarElemental()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        castWithMultikickerPayments(List.of("{G}", "{G}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(2);
        assertThat(wolves).allSatisfy(wolf -> {
            assertThat(wolf.getEffectivePower()).isEqualTo(2);
            assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
        });
    }

    private void castWithMultikickerPayments(List<String> payments) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
    }
}
