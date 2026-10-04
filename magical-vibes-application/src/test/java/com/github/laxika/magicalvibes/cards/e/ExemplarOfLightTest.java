package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.AjaniCallerOfThePride;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnakeskinVeil;
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

@CardUsed({ExemplarOfLight.class, AngelOfMercy.class, Forest.class,
        AjaniCallerOfThePride.class, SnakeskinVeil.class})
class ExemplarOfLightTest extends BaseCardTest {

    private Permanent addExemplar() {
        return harness.addToBattlefieldAndReturn(player1, new ExemplarOfLight());
    }

    private void prepareLibrary(int cardCount) {
        harness.setLibrary(player1, java.util.Collections.nCopies(cardCount, new Forest()));
    }

    private void castAngelAndResolveExemplarTriggers() {
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Gaining life puts a counter on Exemplar of Light and draws a card")
    void gainingLifePutsCounterAndDraws() {
        Permanent exemplar = addExemplar();
        prepareLibrary(1);
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        castAngelAndResolveExemplarTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("The counter trigger draws only once each turn")
    void counterTriggerDrawsOnlyOnceEachTurn() {
        Permanent exemplar = addExemplar();
        prepareLibrary(1);
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        castAngelAndResolveExemplarTriggers();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        castAngelAndResolveExemplarTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void countersFromAnotherAbilityDrawACard() {
        Permanent exemplar = addExemplar();
        prepareLibrary(1);
        harness.setHand(player1, List.of(new SnakeskinVeil()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, exemplar.getId());
        resolveAllTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    void opponentsCounterPlacementDoesNotTriggerDraw() {
        Permanent exemplar = addExemplar();
        prepareLibrary(1);
        harness.setHand(player1, List.of());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniCallerOfThePride());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, 0, null, exemplar.getId());
        resolveAllTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawLimitResetsOnOpponentsTurn() {
        Permanent exemplar = addExemplar();
        prepareLibrary(2);
        harness.setHand(player1, List.of(new SnakeskinVeil()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, exemplar.getId());
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SnakeskinVeil()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, exemplar.getId());
        resolveAllTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsLifeGainDoesNotPutCountersOrDraw() {
        Permanent exemplar = addExemplar();
        prepareLibrary(1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
