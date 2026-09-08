package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        gd.playerDecks.get(player1.getId()).add(new Forest());
        gd.playerDecks.get(player1.getId()).add(new Forest());

        drawAndResolveTrigger(player1.getId());
        drawAndResolveTrigger(player1.getId());

        assertThat(skymage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void drawAndResolveTrigger(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
