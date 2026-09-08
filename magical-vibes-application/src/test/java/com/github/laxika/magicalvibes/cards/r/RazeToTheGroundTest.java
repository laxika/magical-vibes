package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazeToTheGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact with mana value 1 or less and draws a card")
    void destroysLowManaValueArtifactAndDraws() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        castRazeToTheGround(targetId);

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Destroys an artifact with mana value greater than 1 without drawing")
    void destroysHighManaValueArtifactWithoutDrawing() {
        harness.addToBattlefield(player2, new IcyManipulator());
        UUID targetId = harness.getPermanentId(player2, "Icy Manipulator");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        castRazeToTheGround(targetId);

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player2, "Icy Manipulator");
        assertThat(gameData.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        prepareRazeToTheGround();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        FountainOfYouth target = new FountainOfYouth();
        harness.addToBattlefield(player2, target);
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        RazeToTheGround raze = new RazeToTheGround();
        harness.setHand(player1, List.of(raze));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, raze.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castRazeToTheGround(UUID targetId) {
        prepareRazeToTheGround();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareRazeToTheGround() {
        harness.setHand(player1, List.of(new RazeToTheGround()));
        harness.addMana(player1, ManaColor.RED, 3);
    }
}
