package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
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
}
