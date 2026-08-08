package com.github.laxika.magicalvibes.cards.y;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class YomijiWhoBarsTheWayTest extends BaseCardTest {

    @Test
    @DisplayName("A legendary creature the controller owns returns to their hand")
    void allyLegendaryCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, legendaryBears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's legendary permanent returns to its owner's hand, not the controller's")
    void opponentLegendaryReturnsToItsOwnersHand() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, legendaryBears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A legendary noncreature permanent also returns")
    void legendaryArtifactReturns() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        Spellbook legendarySpellbook = new Spellbook();
        legendarySpellbook.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, legendarySpellbook);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spellbook));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("A nonlegendary permanent does not trigger")
    void nonlegendaryPermanentStaysInGraveyard() {
        harness.addToBattlefield(player1, new YomijiWhoBarsTheWay());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Yomiji does not return itself when it dies")
    void yomijiDoesNotReturnItself() {
        Permanent yomiji = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yomiji));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yomiji, Who Bars the Way");
        harness.assertNotInHand(player1, "Yomiji, Who Bars the Way");
    }
}
