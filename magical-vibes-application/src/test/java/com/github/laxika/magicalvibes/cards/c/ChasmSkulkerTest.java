package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChasmSkulkerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Chasm Skulker")
    void drawingCardAddsCounter() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ChasmSkulker());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(skulker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When Chasm Skulker dies, it creates one islandwalking Squid per +1/+1 counter")
    void deathCreatesSquidsForPlusOneCountersOnly() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ChasmSkulker());
        skulker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        skulker.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> squids = findPermanents(player1, "Squid");
        assertThat(squids).hasSize(2);
        assertThat(squids).allSatisfy(squid -> {
            assertThat(gqs.getEffectivePower(gd, squid)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, squid)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, squid, Keyword.ISLANDWALK)).isTrue();
        });
    }
}
