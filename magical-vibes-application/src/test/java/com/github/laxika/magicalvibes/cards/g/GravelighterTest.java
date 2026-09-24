package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("Gravelighter")
@CardUsed({Gravelighter.class, Forest.class, GrizzlyBears.class})
class GravelighterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a creature died this turn")
    void drawsWhenCreatureDiedThisTurn() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravelighter");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Otherwise makes each player sacrifice a creature")
    void otherwiseEachPlayerSacrificesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gravelighter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Gravelighter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
