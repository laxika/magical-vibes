package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TirelessTracker.class, Forest.class, EvolvingWilds.class})
class TirelessTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall investigates — creates a Clue token when controller plays a land")
    void landfallInvestigates() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> clues = findPermanents(player1, "Clue");
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not investigate when opponent plays a land")
    void doesNotTriggerForOpponentLands() {
        harness.addToBattlefield(player1, new TirelessTracker());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Clue");
    }

    @Test
    @DisplayName("Sacrificing a Clue puts a +1/+1 counter on Tireless Tracker")
    void clueSacrificePutsCounter() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());
        addClueToken(player1);

        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));

        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a non-Clue permanent does not put a counter")
    void nonClueSacrificeDoesNotPutCounter() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());

        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Landfall also investigates when a land enters without being played")
    void landEnteringWithoutBeingPlayedInvestigates() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());

        addClueToken(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A tapped Clue can be sacrificed and the counter resolves before its card draw")
    void tappedClueSacrificeTriggersBeforeDraw() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());
        addClueToken(player1);
        Permanent clue = findPermanent(player1, "Clue");
        clue.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent sacrificing a Clue only triggers their own Tracker")
    void opponentClueSacrificeDoesNotTrigger() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());
        Permanent opposingTracker = harness.addToBattlefieldAndReturn(player2, new TirelessTracker());
        addClueToken(player2);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        Permanent clue = findPermanent(player2, "Clue");

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(clue), null, null);
        resolveAllTriggers();

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingTracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Clue sacrifice adds a counter with no once-per-turn limit")
    void multipleClueSacrificesEachPutCounter() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new TirelessTracker());
        addClueToken(player1);
        addClueToken(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 2; i++) {
            Permanent clue = findPermanent(player1, "Clue");
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
            resolveAllTriggers();
        }

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Clue");
    }

    private void addClueToken(Player player) {
        harness.enterBattlefieldAndReturn(player, new Forest());
        resolveAllTriggers();
    }
}
