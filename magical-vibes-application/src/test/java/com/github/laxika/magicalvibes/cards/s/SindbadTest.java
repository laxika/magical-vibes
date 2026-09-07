package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@CardUsed({Sindbad.class, Island.class, GrizzlyBears.class, Abundance.class})
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

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }
}
