package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sindbad.class, Island.class, GrizzlyBears.class, Abundance.class, ThoughtReflection.class})
class SindbadTest extends BaseCardTest {

    @Test
    @DisplayName("Drawn land card is kept in hand")
    void drawnLandIsKept() {
        addCreatureReady(player1, new Sindbad());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertNotInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Drawn nonland card is revealed and discarded")
    void drawnNonlandIsDiscarded() {
        addCreatureReady(player1, new Sindbad());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Grizzly Bears")).isTrue();
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningDrawReplacementStillDiscardsNonland() {
        addCreatureReady(player1, new Sindbad());
        harness.addToBattlefield(player1, new Abundance());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Accepted draw replacement skips Sindbad's reveal and discard")
    void acceptedDrawReplacementSkipsRevealAndDiscard() {
        addCreatureReady(player1, new Sindbad());
        harness.addToBattlefield(player1, new Abundance());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "NONLAND");

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
    @Test
    @DisplayName("Only the drawn copy is discarded when an identical card is already in hand")
    void discardsOnlyTheDrawnCopy() {
        addCreatureReady(player1, new Sindbad());
        GrizzlyBears existing = new GrizzlyBears();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(existing));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(existing);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Drawn land is publicly revealed and activation pays the tap cost")
    void revealsLandAndTapsSindbad() {
        var sindbad = addCreatureReady(player1, new Sindbad());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(sindbad.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Island");
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Island")).isTrue();
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Replacing the draw with two draws skips reveal and discard")
    void doubledDrawSkipsRevealAndDiscard() {
        addCreatureReady(player1, new Sindbad());
        harness.addToBattlefield(player1, new ThoughtReflection());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gameLogContains("reveals Grizzly Bears")).isFalse();
    }
}
