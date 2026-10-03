package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzureMage.class, RuneclawBear.class})
class AzureMageTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability draws a card")
    void drawsACard() {
        harness.addToBattlefield(player1, new AzureMage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly since it has no tap cost")
    void repeatable() {
        harness.addToBattlefield(player1, new AzureMage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void requiresMana() {
        harness.addToBattlefield(player1, new AzureMage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability requires blue mana even with enough generic mana")
    void requiresBlueMana() {
        harness.addToBattlefield(player1, new AzureMage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Mage can activate and draws only on resolution")
    void activatesWhileTappedAndSummoningSick() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new AzureMage());
        mage.tap();
        harness.setHand(player1, List.of());
        RuneclawBear topCard = new RuneclawBear();
        harness.setLibrary(player1, List.of(topCard, new RuneclawBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability draws for its controller when the opponent controls the Mage")
    void drawsForOpponentController() {
        harness.addToBattlefield(player2, new AzureMage());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        RuneclawBear topCard = new RuneclawBear();
        harness.setLibrary(player2, List.of(topCard, new RuneclawBear()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
