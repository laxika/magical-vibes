package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WydwenTheBitingGaleTest extends BaseCardTest {

    @Test
    @DisplayName("{U}{B}, Pay 1 life returns Wydwen to owner's hand")
    void activateReturnsToHand() {
        harness.addToBattlefield(player1, new WydwenTheBitingGale());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wydwen, the Biting Gale"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wydwen, the Biting Gale"));
    }

    @Test
    @DisplayName("Cannot activate ability with no life to pay")
    void cannotActivateWithoutLife() {
        harness.addToBattlefield(player1, new WydwenTheBitingGale());
        harness.setLife(player1, 0);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
