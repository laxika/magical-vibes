package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamunapExcavator.class, Forest.class, Plains.class, GrizzlyBears.class})
class RamunapExcavatorTest extends BaseCardTest {

    @Test
    @DisplayName("Can play a land from graveyard with Ramunap Excavator on battlefield")
    void canPlayLandFromGraveyard() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Playing land from graveyard counts as the land play for the turn")
    void usesNormalLandDrop() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        harness.setGraveyard(player1, List.of(new Forest(), new Plains()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from graveyard without Ramunap Excavator")
    void cannotPlayWithoutExcavator() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Creatures in graveyard are not playable via Ramunap Excavator")
    void creaturesInGraveyardNotPlayable() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Only allows its controller to play lands from graveyard")
    void onlyAffectsController() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    void cannotPlayLandDuringCombat() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotPlayLandDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotPlayLandAfterExcavatorLosesAbilities() {
        harness.addToBattlefieldAndReturn(player1, new RamunapExcavator())
                .setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void handLandUsesTheSameLandAllowance() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Plains()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotPlayLandFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RamunapExcavator());
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
        harness.assertInGraveyard(player2, "Forest");
    }
}
