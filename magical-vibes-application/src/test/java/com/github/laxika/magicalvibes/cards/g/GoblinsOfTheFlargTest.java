package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinsOfTheFlargTest extends BaseCardTest {

    private static Card dwarf() {
        Card card = new Card();
        card.setName("Test Dwarf");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.DWARF));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    @Test
    @DisplayName("Survives while its controller controls no Dwarf")
    void survivesWithoutDwarf() {
        harness.setHand(player1, List.of(new GoblinsOfTheFlarg()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Sacrifices itself when its controller controls a Dwarf")
    void sacrificesWhenControllingDwarf() {
        harness.addToBattlefield(player1, dwarf());
        harness.setHand(player1, List.of(new GoblinsOfTheFlarg()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblins of the Flarg");
        harness.assertInGraveyard(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Does not sacrifice itself for a Dwarf controlled by an opponent")
    void opponentDwarfDoesNotCount() {
        harness.addToBattlefield(player2, dwarf());
        harness.setHand(player1, List.of(new GoblinsOfTheFlarg()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");
    }
}
