package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeaconOfTomorrows.class, Arachnoid.class, Twincast.class, CosisTrickster.class})
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
        harness.addToBattlefield(player2, new Arachnoid());
        harness.setHand(player1, List.of(new BeaconOfTomorrows()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Arachnoid")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a player target")
    void requiresPlayerTarget() {
        harness.setHand(player1, List.of(new BeaconOfTomorrows()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Grants an extra turn to the targeted player")
    void grantsExtraTurnToTargetedPlayer() {
        harness.setHand(player1, List.of(new BeaconOfTomorrows()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

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

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(beacon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(beacon);
    }

    @Test
    @DisplayName("Resolves into an empty library and grants the caster an extra turn")
    void resolvesIntoEmptyLibrary() {
        BeaconOfTomorrows beacon = new BeaconOfTomorrows();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(beacon));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(beacon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(beacon);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("The most recently granted extra turn is taken first")
    void mostRecentlyGrantedExtraTurnIsFirst() {
        harness.setHand(player1, List.of(new BeaconOfTomorrows(), new BeaconOfTomorrows()));
        harness.addMana(player1, ManaColor.BLUE, 16);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.extraTurns).containsExactly(player2.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentTurnIsExtraTurn).isTrue();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("A resolving spell copy still shuffles its owner's library")
    void spellCopyStillShufflesLibrary() {
        BeaconOfTomorrows beacon = new BeaconOfTomorrows();
        harness.addToBattlefield(player2, new CosisTrickster());
        harness.setHand(player1, List.of(beacon, new Twincast()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0, beacon.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(beacon);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isInstanceOf(CosisTrickster.class);
    }
}
