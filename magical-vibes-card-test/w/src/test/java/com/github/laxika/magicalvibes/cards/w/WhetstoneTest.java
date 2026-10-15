package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Whetstone.class)
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
    @DisplayName("Each player mills only the cards available in their library")
    void millsOnlyAvailableCards() {
        harness.setLibrary(player1, List.of(new Whetstone()));
        harness.setLibrary(player2, List.of(new Whetstone()));
        harness.addToBattlefield(player1, new Whetstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
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

    @Test
    @DisplayName("Milling moves the top two cards and leaves the third card in the library")
    void millsTopTwoCards() {
        Whetstone first = new Whetstone();
        Whetstone second = new Whetstone();
        Whetstone third = new Whetstone();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new Whetstone());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Whetstone can be activated repeatedly without tapping")
    void canActivateRepeatedly() {
        Permanent whetstone = harness.addToBattlefieldAndReturn(player1, new Whetstone());
        harness.setLibrary(player1, List.of(new Whetstone(), new Whetstone(), new Whetstone(), new Whetstone()));
        harness.setLibrary(player2, List.of(new Whetstone(), new Whetstone(), new Whetstone(), new Whetstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(whetstone.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An activated ability still resolves after Whetstone leaves the battlefield")
    void resolvesWithoutSource() {
        harness.addToBattlefield(player1, new Whetstone());
        harness.setLibrary(player1, List.of(new Whetstone(), new Whetstone()));
        harness.setLibrary(player2, List.of(new Whetstone(), new Whetstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
