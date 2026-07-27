package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SharuumTheHegemonTest extends BaseCardTest {

    /** Casts Sharuum and resolves the creature spell so its ETB trigger sets up graveyard targeting. */
    private void castSharuum() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SharuumTheHegemon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    @Test
    @DisplayName("ETB returns a targeted artifact card from graveyard to the battlefield")
    void etbReturnsArtifactToBattlefield() {
        AmaranthineWall wall = new AmaranthineWall();
        harness.setGraveyard(player1, List.of(wall));

        castSharuum();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(wall.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability

        harness.assertOnBattlefield(player1, "Amaranthine Wall");
        harness.assertNotInGraveyard(player1, "Amaranthine Wall");
    }

    @Test
    @DisplayName("A non-artifact card in the graveyard is not a legal target")
    void nonArtifactNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castSharuum();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        AmaranthineWall wall = new AmaranthineWall();
        harness.setGraveyard(player1, List.of(wall));

        castSharuum();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // Choose nothing — "you may return"
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Amaranthine Wall");
        harness.assertNotOnBattlefield(player1, "Amaranthine Wall");
    }

    @Test
    @DisplayName("Empty graveyard produces no trigger")
    void emptyGraveyardNoTrigger() {
        castSharuum();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
