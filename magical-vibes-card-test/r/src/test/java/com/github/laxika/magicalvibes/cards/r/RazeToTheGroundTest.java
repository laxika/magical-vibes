package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.MishrasResearchDesk;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazeToTheGround.class, FountainOfYouth.class, GrizzlyBears.class, IcyManipulator.class,
        Cancel.class, MishrasResearchDesk.class, Naturalize.class, WheelOfSunAndMoon.class})
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
        harness.castAndResolveInstant(player2, 0, raze.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws for an artifact with mana value exactly one")
    void drawsForManaValueOneArtifact() {
        harness.addToBattlefield(player2, new MishrasResearchDesk());
        UUID targetId = harness.getPermanentId(player2, "Mishra's Research Desk");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castRazeToTheGround(targetId);

        harness.assertInGraveyard(player2, "Mishra's Research Desk");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Does not draw when its only target leaves the battlefield before resolution")
    void doesNotDrawWhenTargetBecomesIllegal() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        prepareRazeToTheGround();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Raze to the Ground");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Destroys the artifact before drawing with Wheel of Sun and Moon")
    void destroysBeforeDrawingWithGraveyardReplacement() {
        harness.setHand(player1, List.of(new WheelOfSunAndMoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.setLibrary(player1, List.of());

        castRazeToTheGround(targetId);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        harness.assertInHand(player1, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castRazeToTheGround(UUID targetId) {
        prepareRazeToTheGround();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void prepareRazeToTheGround() {
        harness.setHand(player1, List.of(new RazeToTheGround()));
        harness.addMana(player1, ManaColor.RED, 3);
    }
}
