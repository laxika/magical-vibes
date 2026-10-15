package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScholarOfStars.class, Manalith.class, Forest.class, Disperse.class})
class ScholarOfStarsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card on ETB when controlling an artifact")
    void drawsWithArtifact() {
        harness.addToBattlefield(player1, new Manalith());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarOfStars(), "{3}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scholar of Stars");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw on ETB without an artifact")
    void noDrawWithoutArtifact() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarOfStars(), "{3}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scholar of Stars");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's artifact does not satisfy the condition")
    void opponentArtifactDoesNotCount() {
        harness.addToBattlefield(player2, new Manalith());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarOfStars(), "{3}{U}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw if the last artifact leaves before the trigger resolves")
    void noDrawAfterLastArtifactLeaves() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new Manalith());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarOfStars(), "{3}{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.assertNotOnBattlefield(player1, "Manalith");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Manalith");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Still draws if another artifact remains when the trigger resolves")
    void drawsWithAnotherArtifactRemaining() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new Manalith());
        harness.addToBattlefield(player1, new Manalith());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, new ScholarOfStars(), "{3}{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Manalith");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gaining an artifact after entry does not create a draw trigger")
    void artifactMustBeControlledAtEntry() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new ScholarOfStars(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new Manalith());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
