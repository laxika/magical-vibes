package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfbriarElemental.class})
class WolfbriarElementalTest extends BaseCardTest {

    @Test
    void createsNoWolvesWithoutMultikicker() {
        harness.setHand(player1, List.of(new WolfbriarElemental()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void createsOneWolfPerMultikickerPayment() {
        harness.setHand(player1, List.of(new WolfbriarElemental()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{G}", "{G}"));
        resolveAllTriggers();

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(2);
        assertThat(wolves).allSatisfy(wolf -> {
            assertThat(wolf.getEffectivePower()).isEqualTo(2);
            assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    void createsOneWolfOnlyAfterTheEntryTriggerResolves() {
        harness.setHand(player1, List.of(new WolfbriarElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{G}"));
        assertThat(findPermanents(player1, "Wolf")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Wolfbriar Elemental")).hasSize(1);
        assertThat(findPermanents(player1, "Wolf")).isEmpty();

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        assertThat(findPermanents(player2, "Wolf")).isEmpty();
    }

    @Test
    void cannotPayMultikickerWithColorlessMana() {
        harness.setHand(player1, List.of(new WolfbriarElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{G}")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }
}
