package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhetstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Each player mills two cards when the ability resolves")
    void eachPlayerMillsTwoCards() {
        harness.addToBattlefield(player1, new Whetstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int player1DeckSize = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(player1DeckSize - 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(player2DeckSize - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability does not require tapping Whetstone")
    void abilityDoesNotRequireTapping() {
        Permanent whetstone = harness.addToBattlefieldAndReturn(player1, new Whetstone());
        whetstone.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without three mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new Whetstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
