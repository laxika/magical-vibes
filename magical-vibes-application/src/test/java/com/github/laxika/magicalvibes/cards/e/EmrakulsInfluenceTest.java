package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WretchedGryff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmrakulsInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when a controller casts an Eldrazi creature with mana value 7")
    void drawsTwoCardsForLargeEldraziCreatureSpell() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        harness.setHand(player1, List.of(new WretchedGryff()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger for an Eldrazi creature with mana value less than 7")
    void doesNotTriggerForSmallEldraziCreatureSpell() {
        harness.addToBattlefield(player1, new EmrakulsInfluence());
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new EmrakulsEvangel()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }
}
