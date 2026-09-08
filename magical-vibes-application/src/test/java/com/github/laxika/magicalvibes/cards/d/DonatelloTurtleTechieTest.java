package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonatelloTurtleTechie.class, Spellbook.class, Forest.class})
class DonatelloTurtleTechieTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card on ETB when controlling an artifact")
    void drawsWithArtifact() {
        harness.addToBattlefield(player1, new Spellbook());
        prepareDeckAndHand();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Donatello, Turtle Techie");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw on ETB without an artifact")
    void noDrawWithoutArtifact() {
        prepareDeckAndHand();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Donatello, Turtle Techie");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's artifact does not satisfy the condition")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player2, new Spellbook());
        prepareDeckAndHand();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void prepareDeckAndHand() {
        harness.setHand(player1, List.of(new DonatelloTurtleTechie()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
    }
}
