package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rejuvenate.class, BlanchwoodTreefolk.class})
class RejuvenateTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 6 life")
    void gainsSixLife() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new Rejuvenate(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cycling discards Rejuvenate and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Rejuvenate()));
        harness.setLibrary(player1, List.of(new BlanchwoodTreefolk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rejuvenate");
        harness.assertInHand(player1, "Blanchwood Treefolk");
    }

    @Test
    @DisplayName("Life gain can exceed the starting life total and affects only the caster")
    void lifeGainCanExceedStartingTotal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 13);
        harness.castFromHand(player1, new Rejuvenate(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player1, "Rejuvenate");
    }

    @Test
    @DisplayName("Cycling discards as a cost, draws only on resolution, and does not gain life")
    void cyclingDiscardsBeforeDrawingWithoutGainingLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new Rejuvenate()));
        harness.setLibrary(player1, List.of(new BlanchwoodTreefolk()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Rejuvenate");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Blanchwood Treefolk");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new Rejuvenate()));
        harness.setLibrary(player1, List.of(new BlanchwoodTreefolk()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Rejuvenate");
        harness.assertNotInHand(player1, "Blanchwood Treefolk");
        harness.assertNotInGraveyard(player1, "Rejuvenate");
        assertThat(gd.stack).isEmpty();
    }
}
