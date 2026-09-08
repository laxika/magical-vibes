package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pyromania.class, Forest.class})
class PyromaniaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and discards a random card as a cost")
    void discardsRandomCardAndDealsDamage() {
        harness.addToBattlefield(player1, new Pyromania());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 1 damage")
    void sacrificesItselfAndDealsDamage() {
        harness.addToBattlefield(player1, new Pyromania());
        harness.setLife(player2, 20);
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Pyromania");
        harness.assertInGraveyard(player1, "Pyromania");
    }

    @Test
    @DisplayName("Cannot use the discard ability with an empty hand")
    void cannotDiscardWithEmptyHand() {
        harness.addToBattlefield(player1, new Pyromania());
        harness.setHand(player1, List.of());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
