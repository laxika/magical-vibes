package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeaconOfTomorrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting targets a player")
    void castingTargetsPlayer() {
        BeaconOfTomorrows beacon = new BeaconOfTomorrows();
        harness.setHand(player1, List.of(beacon));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BeaconOfTomorrows()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Grants an extra turn to the targeted player")
    void grantsExtraTurnToTargetedPlayer() {
        harness.setHand(player1, List.of(new BeaconOfTomorrows()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().extraTurns).containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library after resolving")
    void shufflesItselfIntoOwnersLibrary() {
        BeaconOfTomorrows beacon = new BeaconOfTomorrows();
        harness.setHand(player1, List.of(beacon));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(beacon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(beacon);
    }
}
