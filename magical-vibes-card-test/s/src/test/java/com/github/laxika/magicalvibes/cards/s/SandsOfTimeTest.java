package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.cards.c.CityOfSolitude;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.cards.t.TitaniasSong;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Breezekeeper.class, CityOfSolitude.class, Quicksand.class, SandsOfTime.class, TitaniasSong.class,
        Warthog.class})
class SandsOfTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped non-A/C/L permanents stay tapped through the skipped untap step")
    void skipsUntapStep() {
        addCreatureReady(player1, new SandsOfTime());
        // Enchantments are not flipped by the upkeep ability — so a tapped enchantment staying
        // tapped proves the untap step itself was skipped (advanceToNextTurn auto-resolves upkeep).
        Permanent cityOfSolitude = addCreatureReady(player1, new CityOfSolitude());
        cityOfSolitude.tap();

        advanceToNextTurn(player2);

        assertThat(cityOfSolitude.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Skipping the untap step prevents phasing")
    void skipPreventsPhasing() {
        addCreatureReady(player1, new SandsOfTime());
        Permanent keeper = addCreatureReady(player1, new Breezekeeper());

        advanceToNextTurn(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(keeper);
    }

    @Test
    @DisplayName("Losing Sands of Time's abilities stops its untap-step effect")
    void losingAbilitiesStopsUntapStepEffect() {
        addCreatureReady(player1, new SandsOfTime());
        addCreatureReady(player1, new TitaniasSong());
        Permanent cityOfSolitude = addCreatureReady(player1, new CityOfSolitude());
        cityOfSolitude.tap();

        advanceToNextTurn(player2);

        assertThat(cityOfSolitude.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Losing Sands of Time's abilities stops its upkeep trigger")
    void losingAbilitiesStopsUpkeepTrigger() {
        addCreatureReady(player1, new SandsOfTime());
        addCreatureReady(player1, new TitaniasSong());
        Permanent warthog = addCreatureReady(player1, new Warthog());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(warthog.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Upkeep simultaneously flips tap states of artifacts, creatures, and lands")
    void upkeepFlipsTapStates() {
        Permanent sands = addCreatureReady(player1, new SandsOfTime());
        Permanent tappedWarthog = addCreatureReady(player1, new Warthog());
        Permanent untappedWarthog = addCreatureReady(player1, new Warthog());
        Permanent tappedQuicksand = addCreatureReady(player1, new Quicksand());
        Permanent untappedQuicksand = addCreatureReady(player1, new Quicksand());
        Permanent cityOfSolitude = addCreatureReady(player1, new CityOfSolitude());
        tappedWarthog.tap();
        tappedQuicksand.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(tappedWarthog.isTapped()).isFalse();
        assertThat(untappedWarthog.isTapped()).isTrue();
        assertThat(tappedQuicksand.isTapped()).isFalse();
        assertThat(untappedQuicksand.isTapped()).isTrue();
        assertThat(sands.isTapped()).isTrue();
        assertThat(cityOfSolitude.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's upkeep flips only that player's matching permanents")
    void flipsOnlyActivePlayersPermanents() {
        addCreatureReady(player1, new SandsOfTime());
        Permanent ownWarthog = addCreatureReady(player1, new Warthog());
        Permanent opponentWarthog = addCreatureReady(player2, new Warthog());
        ownWarthog.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(ownWarthog.isTapped()).isTrue();
        assertThat(opponentWarthog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still skips untap and flips while Sands of Time itself is tapped")
    void worksWhileTapped() {
        Permanent sands = addCreatureReady(player1, new SandsOfTime());
        Permanent cityOfSolitude = addCreatureReady(player1, new CityOfSolitude());
        Permanent warthog = addCreatureReady(player1, new Warthog());
        sands.tap();
        cityOfSolitude.tap();
        warthog.tap();

        advanceToNextTurn(player2);
        assertThat(cityOfSolitude.isTapped()).isTrue(); // skip still applied while Sands is tapped
        assertThat(warthog.isTapped()).isFalse(); // upkeep flip already resolved via auto-pass
        assertThat(sands.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Upkeep uses tap states at resolution rather than when the ability triggers")
    void usesTapStatesAtResolution() {
        addCreatureReady(player1, new SandsOfTime());
        Permanent warthog = addCreatureReady(player1, new Warthog());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        warthog.tap();
        harness.passBothPriorities();

        assertThat(warthog.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An upkeep trigger still resolves after Sands of Time leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        Permanent sands = addCreatureReady(player1, new SandsOfTime());
        Permanent warthog = addCreatureReady(player1, new Warthog());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sands);
        gd.playerGraveyards.get(player1.getId()).add(sands.getCard());
        harness.passBothPriorities();

        assertThat(warthog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two copies each trigger and flip permanents back to their original states")
    void multipleCopiesFlipTwice() {
        Permanent firstSands = addCreatureReady(player1, new SandsOfTime());
        Permanent secondSands = addCreatureReady(player2, new SandsOfTime());
        Permanent warthog = addCreatureReady(player1, new Warthog());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(firstSands.isTapped()).isFalse();
        assertThat(secondSands.isTapped()).isFalse();
        assertThat(warthog.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
