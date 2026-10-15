package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClinquantSkymage.class, Forest.class})
class ClinquantSkymageTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Clinquant Skymage")
    void drawingCardAddsCounter() {
        Permanent skymage = harness.addToBattlefieldAndReturn(player1, new ClinquantSkymage());
        gd.playerDecks.get(player1.getId()).add(new Forest());

        drawAndResolveTrigger(player1.getId());

        assertThat(skymage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each card drawn puts a separate +1/+1 counter on Clinquant Skymage")
    void eachDrawAddsCounter() {
        Permanent skymage = harness.addToBattlefieldAndReturn(player1, new ClinquantSkymage());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        drawAndResolveTrigger(player1.getId());
        drawAndResolveTrigger(player1.getId());

        assertThat(skymage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent draws do not put counters on Clinquant Skymage")
    void opponentDrawDoesNotAddCounter() {
        Permanent skymage = harness.addToBattlefieldAndReturn(player1, new ClinquantSkymage());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(skymage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Drawing multiple cards creates a separate trigger for each card")
    void multipleCardsDrawnTogetherTriggerSeparately() {
        Permanent skymage = harness.addToBattlefieldAndReturn(player1, new ClinquantSkymage());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.stack).hasSize(2);
        assertThat(skymage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(skymage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(skymage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Clinquant Skymage gets its own counter when its controller draws")
    void multipleSkymagesEachGetCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ClinquantSkymage());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ClinquantSkymage());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ClinquantSkymage());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A pending draw trigger does not put a counter on a replacement Skymage")
    void departedSourceDoesNotPutCounterOnAnotherSkymage() {
        ClinquantSkymage card = new ClinquantSkymage();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void drawAndResolveTrigger(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
