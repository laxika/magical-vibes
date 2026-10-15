package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PaperfinRascal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogHoodlums.class, PaperfinRascal.class, Forest.class})
class BogHoodlumsTest extends BaseCardTest {

    private Permanent castBogHoodlums() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BogHoodlums(), "{5}{B}");
        resolveAllTriggers();
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            gs.handleInteractionAnswer(gd, scry.playerId().equals(player1.getId()) ? player1 : player2,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }

        return findPermanent(player1, "Bog Hoodlums");
    }

    // ===== Won clash — put a +1/+1 counter on it =====

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Bog Hoodlums")
    void wonClashAddsCounter() {
        harness.setLibrary(player1, List.of(new PaperfinRascal()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent hoodlums = castBogHoodlums();

        assertThat(hoodlums.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hoodlums.getEffectivePower()).isEqualTo(5);
        assertThat(hoodlums.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Lost clash — no counter =====

    @Test
    @DisplayName("Losing the clash leaves Bog Hoodlums without a counter")
    void lostClashAddsNoCounter() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent hoodlums = castBogHoodlums();

        assertThat(hoodlums.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(hoodlums.getEffectivePower()).isEqualTo(4);
        assertThat(hoodlums.getEffectiveToughness()).isEqualTo(1);
    }

    // ===== Tie — a clash is only won on a strictly greater mana value (CR 701.30d) =====

    @Test
    @DisplayName("An equal mana value tie is not a win, so no counter is added")
    void tiedClashAddsNoCounter() {
        harness.setLibrary(player1, List.of(new PaperfinRascal()));
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent hoodlums = castBogHoodlums();

        assertThat(hoodlums.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("An empty library cannot win the clash, so no counter is added")
    void emptyLibraryAddsNoCounter() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent hoodlums = castBogHoodlums();

        assertThat(hoodlums.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(hoodlums.getEffectivePower()).isEqualTo(4);
        assertThat(hoodlums.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing a land wins when the opponent reveals no card")
    void opponentEmptyLibraryStillAllowsWin() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());

        Permanent hoodlums = castBogHoodlums();

        assertThat(hoodlums.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Neither player wins when both libraries are empty")
    void bothEmptyLibrariesAddNoCounter() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        Permanent hoodlums = castBogHoodlums();

        assertThat(hoodlums.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Clash asks players where to put their revealed cards")
    void clashOffersLibraryPlacementChoice() {
        harness.setLibrary(player1, List.of(new PaperfinRascal(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new PaperfinRascal()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BogHoodlums(), "{5}{B}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    // ===== Can't block =====

    @Test
    @DisplayName("Bog Hoodlums cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new BogHoodlums());

        Permanent attacker = addCreatureReady(player1, new BogHoodlums());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
