package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({LorescaleCoatl.class})
class LorescaleCoatlTest extends BaseCardTest {

    @Test
    @DisplayName("Draw step draw puts a +1/+1 counter on Lorescale Coatl")
    void triggersOnDrawStepDraw() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(coatl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({CounselOfTheSoratami.class})
    @DisplayName("Drawing multiple cards from a spell adds one counter per card drawn")
    void triggersOncePerCardDrawn() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Counsel of the Soratami draws 2 cards.
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(coatl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent drawing a card does not trigger Lorescale Coatl")
    void doesNotTriggerOnOpponentDraw() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());

        advanceToDraw(player2);

        assertThat(coatl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each Coatl gets its own counter when their controller draws")
    void eachCoatlTriggersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LorescaleCoatl());

        advanceToDraw(player1);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The second player's Coatl triggers on that player's draw")
    void triggersForSecondPlayer() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player2, new LorescaleCoatl());

        advanceToDraw(player2);
        resolveAllTriggers();

        assertThat(coatl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({Unsummon.class})
    @DisplayName("A pending draw trigger does not put counters on another Coatl after its source leaves")
    void removedSourceDoesNotRedirectCounter() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        advanceToDraw(player1);

        harness.castAndResolveInstant(player1, 0, source.getId());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LorescaleCoatl());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotInGraveyard(player1, "Lorescale Coatl");
        harness.assertInHand(player1, "Lorescale Coatl");
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }
}
