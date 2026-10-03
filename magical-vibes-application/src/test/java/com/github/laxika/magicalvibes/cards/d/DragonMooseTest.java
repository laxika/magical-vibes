package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonMoose.class})
class DragonMooseTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Dragon Moose to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.setHand(player1, List.of(new DragonMoose()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Haste does not allow a tapped Dragon Moose to attack")
    void hasteDoesNotAllowAttackingWhileTapped() {
        harness.setHand(player1, List.of(new DragonMoose()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Dragon Moose").setTapped(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        harness.assertLife(player2, 20);
    }
}
