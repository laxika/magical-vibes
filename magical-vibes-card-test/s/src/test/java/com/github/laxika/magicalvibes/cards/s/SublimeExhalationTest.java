package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SublimeExhalation.class, GrizzlyBears.class, Swamp.class})
class SublimeExhalationTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less per opponent and destroys all creatures")
    void reducesCostAndDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Swamp());
        harness.castFromHand(player1, new SublimeExhalation(), "{5}{W}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Sublime Exhalation");
    }

    @Test
    @DisplayName("One opponent does not reduce the generic cost below five")
    void cannotCastWithTooLittleGenericMana() {
        harness.setHand(player1, List.of(new SublimeExhalation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Sublime Exhalation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undaunted does not reduce the white mana requirement")
    void stillRequiresWhiteMana() {
        harness.setHand(player1, List.of(new SublimeExhalation()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Sublime Exhalation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can resolve with no creatures on the battlefield")
    void resolvesWithoutCreatures() {
        harness.addToBattlefield(player2, new Swamp());
        harness.castFromHand(player1, new SublimeExhalation(), "{5}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Swamp");
        harness.assertInGraveyard(player1, "Sublime Exhalation");
        assertThat(gd.stack).isEmpty();
    }
}
