package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonKnightsOfRound.class, GrizzlyBears.class})
class SummonKnightsOfRoundTest extends BaseCardTest {

    @Test
    void chaptersICreatesThreeKnightTokens() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonKnightsOfRound());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findKnightTokens()).hasSize(3);
        assertThat(findKnightTokens()).allSatisfy(knight -> {
            assertThat(knight.getEffectivePower()).isEqualTo(2);
            assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    void chapterVBoostsAndGivesIndestructibleCountersToOtherControlledCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonKnightsOfRound());
        saga.setCounterCount(CounterType.LORE, 4);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void castingCreatesKnightsAndAllFourTokenChaptersAccumulateBeforeUltimateEnd() {
        harness.castFromHand(player1, new SummonKnightsOfRound(), "{6}{W}{W}");
        harness.passBothPriorities();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(findKnightTokens()).isEmpty();
        harness.passBothPriorities();

        assertThat(findKnightTokens()).hasSize(3);
        for (int chapter = 2; chapter <= 4; chapter++) {
            advanceToNextChapter();
            harness.passBothPriorities();

            assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(chapter);
            assertThat(findKnightTokens()).hasSize(chapter * 3);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        }
        assertThat(findKnightTokens()).allSatisfy(knight -> {
            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
            assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(knight.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
            assertThat(gqs.isCreature(gd, knight)).isTrue();
        });

        advanceToNextChapter();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.passBothPriorities();

        assertThat(findKnightTokens()).hasSize(12).allSatisfy(knight -> {
            assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
            assertThat(knight.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
        });
        assertThat(saga.getPowerModifier()).isZero();
        assertThat(saga.getToughnessModifier()).isZero();
        assertThat(saga.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        harness.assertInGraveyard(player1, "Summon: Knights of Round");
    }

    @Test
    void ultimateEndDoesNotAffectCreaturesEnteringAfterResolution() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonKnightsOfRound());
        saga.setCounterCount(CounterType.LORE, 4);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new SummonKnightsOfRound());
        assertThat(lateCreature.getPowerModifier()).isZero();
        assertThat(lateCreature.getToughnessModifier()).isZero();
        assertThat(lateCreature.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        harness.assertInGraveyard(player1, "Summon: Knights of Round");
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    private java.util.List<Permanent> findKnightTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
