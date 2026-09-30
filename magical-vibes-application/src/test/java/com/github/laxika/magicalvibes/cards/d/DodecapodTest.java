package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.m.MindlessAutomaton;
import com.github.laxika.magicalvibes.cards.s.Stupor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dodecapod.class, FuneralCharm.class, Stupor.class, MindlessAutomaton.class})
class DodecapodTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters when randomly discarded by an opponent's spell")
    void entersWithCountersWhenRandomlyDiscardedByOpponentSpell() {
        harness.setHand(player1, List.of(new Dodecapod()));
        harness.setHand(player2, List.of(new Stupor()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        Permanent dodecapod = findPermanent(player1, "Dodecapod");
        assertThat(dodecapod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Dodecapod");
    }

    @Test
    @DisplayName("Enters with two +1/+1 counters when discarded by an opponent's targeted spell")
    void entersWithCountersWhenDiscardedByOpponentTargetedSpell() {
        harness.setHand(player1, List.of(new Dodecapod()));
        harness.setHand(player2, List.of(new FuneralCharm()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent dodecapod = findPermanent(player1, "Dodecapod");
        assertThat(dodecapod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Dodecapod");
    }

    @Test
    @DisplayName("Goes to the graveyard when its controller causes it to be discarded")
    void remainsInGraveyardWhenDiscardedByController() {
        Permanent automaton = addCreatureReady(player1, new MindlessAutomaton());
        automaton.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Dodecapod()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dodecapod");
        harness.assertInGraveyard(player1, "Dodecapod");
        assertThat(automaton.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not enter with counters when cast normally")
    void normalCastHasNoCounters() {
        harness.castFromHand(player1, new Dodecapod(), "{4}");
        harness.passBothPriorities();

        Permanent dodecapod = findPermanent(player1, "Dodecapod");
        assertThat(dodecapod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
