package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PalaceFamiliar.class, GrizzlyBears.class, Shock.class})
class PalaceFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("When Palace Familiar dies, its controller draws a card")
    void diesDrawsCard() {
        Permanent familiar = addCreatureReady(player1, new PalaceFamiliar());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        killWithShock(familiar);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Palace Familiar does not trigger when another creature dies")
    void anotherCreatureDiesDoesNotDraw() {
        addCreatureReady(player1, new PalaceFamiliar());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        killWithShock(otherCreature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("The death trigger waits to resolve and draws only for the Familiar's controller")
    void opposingControllerDrawsOnlyWhenDeathTriggerResolves() {
        Permanent familiar = addCreatureReady(player2, new PalaceFamiliar());
        PalaceFamiliar drawnCard = new PalaceFamiliar();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setLibrary(player1, List.of(new PalaceFamiliar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, familiar.getId());

        harness.assertNotOnBattlefield(player2, "Palace Familiar");
        harness.assertInGraveyard(player2, "Palace Familiar");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize + 1)
                .contains(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
