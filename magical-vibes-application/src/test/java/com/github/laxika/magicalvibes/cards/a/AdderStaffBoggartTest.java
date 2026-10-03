package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdderStaffBoggart.class, Forest.class, GrizzlyBears.class})
class AdderStaffBoggartTest extends BaseCardTest {

    private Permanent castAdderStaffBoggart() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new AdderStaffBoggart(), "{1}{R}");
        harness.passBothPriorities(); // resolve creature spell (ETB clash trigger placed)
        harness.passBothPriorities(); // resolve ETB clash effect

        return findPermanent(player1, "Adder-Staff Boggart");
    }

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Adder-Staff Boggart")
    void wonClashAddsCounter() {
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Permanent boggart = castAdderStaffBoggart();

        assertThat(boggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(boggart.getEffectivePower()).isEqualTo(3);
        assertThat(boggart.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing the clash leaves Adder-Staff Boggart without a counter")
    void lostClashAddsNoCounter() {
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent boggart = castAdderStaffBoggart();

        assertThat(boggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(boggart.getEffectivePower()).isEqualTo(2);
        assertThat(boggart.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("An equal mana value tie is not a win, so no counter is added")
    void tiedClashAddsNoCounter() {
        // Equal mana values (both Grizzly Bears MV 2) → no one wins the clash.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent boggart = castAdderStaffBoggart();

        assertThat(boggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Clashing players must be offered the choice to bottom their revealed cards")
    void clashOffersLibraryPlacementChoice() {
        harness.setLibrary(player1, List.of(new AdderStaffBoggart(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new AdderStaffBoggart()));

        castAdderStaffBoggart();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Revealing a land wins against an opponent who reveals no card")
    void landWinsAgainstEmptyLibrary() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());

        Permanent boggart = castAdderStaffBoggart();

        assertThat(boggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An empty controller library cannot win a clash")
    void emptyControllerLibraryDoesNotWin() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent boggart = castAdderStaffBoggart();

        assertThat(boggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Neither player wins when both libraries are empty")
    void bothLibrariesEmptyDoesNotWin() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent boggart = castAdderStaffBoggart();

        assertThat(boggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
