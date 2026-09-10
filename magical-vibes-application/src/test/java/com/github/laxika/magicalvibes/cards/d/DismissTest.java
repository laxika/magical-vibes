package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.Grindstone;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dismiss.class, HornedTurtle.class, Grindstone.class})
class DismissTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the targeted creature spell and draws a card")
    void countersCreatureSpellAndDraws() {
        HornedTurtle turtle = new HornedTurtle();
        harness.setHand(player1, List.of(turtle));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Dismiss()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, turtle.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Horned Turtle");
        harness.assertNotOnBattlefield(player1, "Horned Turtle");
        harness.assertInGraveyard(player2, "Dismiss");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a non-creature spell too")
    void countersNoncreatureSpell() {
        Grindstone grindstone = new Grindstone();
        harness.setHand(player1, List.of(grindstone));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Dismiss()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, grindstone.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grindstone");
        harness.assertNotOnBattlefield(player1, "Grindstone");

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when the targeted spell leaves the stack first")
    void doesNotDrawWhenTargetSpellLeavesStack() {
        HornedTurtle turtle = new HornedTurtle();
        harness.setHand(player1, List.of(turtle));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Dismiss dismiss = new Dismiss();
        harness.setHand(player2, List.of(dismiss));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, turtle.getId());
        gd.stack.removeIf(stackEntry -> stackEntry.getCard().getId().equals(turtle.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Dismiss");
        assertThat(gd.stack).isEmpty();
    }
}
