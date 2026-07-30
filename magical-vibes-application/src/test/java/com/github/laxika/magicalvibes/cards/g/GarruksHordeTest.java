package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarruksHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a creature spell from the top of the library")
    void castsCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot cast a creature from the top without Garruk's Horde on the battlefield")
    void cannotCastFromTopWithoutHorde() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Cannot cast a noncreature spell from the top of the library")
    void cannotCastNoncreatureFromTop() {
        harness.addToBattlefield(player1, new GarruksHorde());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }
}
