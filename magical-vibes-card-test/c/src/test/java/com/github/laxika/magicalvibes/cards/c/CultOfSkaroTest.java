package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdricMathematicalGenius;
import com.github.laxika.magicalvibes.cards.s.SontaranGeneral;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CultOfSkaro.class, SontaranGeneral.class, AdricMathematicalGenius.class})
class CultOfSkaroTest extends BaseCardTest {

    @Test
    void attackingResolvesExactlyOneRandomMode() {
        harness.setLibrary(player1, List.of(new SontaranGeneral(), new SontaranGeneral()));

        Permanent cult = addCreatureReady(player1, new CultOfSkaro());
        int initialHandSize = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        int resolvedModes = 0;
        if (cult.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1) {
            resolvedModes++;
        }
        if (gd.playerHands.get(player1.getId()).size() == initialHandSize + 2) {
            resolvedModes++;
        }
        if (countPermanents(player1, "Dalek") == 1) {
            resolvedModes++;
        }
        if (gd.getLife(player2.getId()) == 12) {
            resolvedModes++;
        }

        assertThat(resolvedModes)
                .withFailMessage("counter=%s, hand=%s, daleks=%s, opponentLife=%s",
                        cult.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE),
                        gd.playerHands.get(player1.getId()).size(),
                        countPermanents(player1, "Dalek"),
                        gd.getLife(player2.getId()))
                .isEqualTo(1);
    }

    @RepeatedTest(16)
    void copiedAttackTriggerRetainsTheOriginalRandomMode() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new SontaranGeneral(), new SontaranGeneral(),
                new SontaranGeneral(), new SontaranGeneral()));
        Permanent cult = addCreatureReady(player1, new CultOfSkaro());
        Permanent adric = addCreatureReady(player1, new AdricMathematicalGenius());
        Permanent nonartifact = addCreatureReady(player1, new SontaranGeneral());
        Permanent opposingArtifact = addCreatureReady(player2, new CultOfSkaro());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.addMana(player1, ManaColor.BLUE, 3);
            harness.activateAbility(player1, 1, null, gd.stack.getLast().getTargetableId());
            resolveAllTriggers();
        });

        int resolvedModes = 0;
        if (cult.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2) {
            resolvedModes++;
        }
        if (gd.playerHands.get(player1.getId()).size() == 4) {
            resolvedModes++;
        }
        if (countPermanents(player1, "Dalek") == 2) {
            resolvedModes++;
        }
        if (gd.getLife(player2.getId()) == 12) {
            resolvedModes++;
        }
        assertThat(resolvedModes)
                .withFailMessage("A copy must repeat the original mode: counters=%s, hand=%s, tokens=%s, life=%s",
                        cult.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE),
                        gd.playerHands.get(player1.getId()).size(),
                        countPermanents(player1, "Dalek"), gd.getLife(player2.getId()))
                .isEqualTo(1);
        assertThat(adric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonartifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArtifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        for (Permanent dalek : findPermanents(player1, "Dalek")) {
            assertThat(gqs.getEffectivePower(gd, dalek)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, dalek)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, dalek, Keyword.MENACE)).isTrue();
            assertThat(dalek.isTapped()).isFalse();
        }
    }

    @Test
    void doesNotTriggerWhenOnlyAnotherCreatureAttacks() {
        Permanent cult = addCreatureReady(player1, new CultOfSkaro());
        addCreatureReady(player1, new SontaranGeneral());
        int initialHandSize = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });

        assertThat(cult.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialHandSize);
        assertThat(countPermanents(player1, "Dalek")).isZero();
        harness.assertLife(player2, 20);
    }
}
