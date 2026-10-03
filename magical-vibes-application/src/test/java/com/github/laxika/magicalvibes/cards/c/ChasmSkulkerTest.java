package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.i.InGarruksWake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChasmSkulker.class, Divination.class, FleshToDust.class, InGarruksWake.class})
class ChasmSkulkerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on Chasm Skulker")
    void drawingCardAddsCounter() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ChasmSkulker());
        gd.playerDecks.get(player1.getId()).add(new ChasmSkulker());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(skulker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

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
        harness.setHand(player2, List.of(new InGarruksWake()));
        harness.addMana(player2, ManaColor.BLACK, 9);
        harness.castAndResolveSorcery(player2, 0, 0);
        harness.passBothPriorities();

        List<Permanent> squids = findPermanents(player1, "Squid");
        assertThat(squids).hasSize(2);
        assertThat(squids).allSatisfy(squid -> {
            assertThat(gqs.getEffectivePower(gd, squid)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, squid)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, squid, Keyword.ISLANDWALK)).isTrue();
        });
    }

    @Test
    void drawingTwoCardsCreatesSeparateCounterTriggers() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ChasmSkulker());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new ChasmSkulker(), new ChasmSkulker()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(skulker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(skulker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(skulker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentDrawDoesNotAddCounter() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ChasmSkulker());
        gd.playerDecks.get(player2.getId()).add(new ChasmSkulker());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(skulker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathBeforeDrawTriggerResolvesUsesOnlyExistingCounters() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ChasmSkulker());
        skulker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerDecks.get(player1.getId()).add(new ChasmSkulker());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player2, 0, skulker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Chasm Skulker")).isEmpty();
        assertThat(findPermanents(player1, "Squid")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathWithoutCountersStillTriggersButCreatesNoTokens() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ChasmSkulker());
        harness.setHand(player1, List.of(new FleshToDust()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, skulker.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Squid")).isEmpty();
    }
}
