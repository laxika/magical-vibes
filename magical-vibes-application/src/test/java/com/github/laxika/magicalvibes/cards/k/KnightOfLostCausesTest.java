package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfLostCauses.class, GrizzlyBears.class, MishrasFactory.class})
class KnightOfLostCausesTest extends BaseCardTest {

    @Test
    void getsBoostWhenOpponentHasTenMoreLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        Permanent knight = addKnight();

        assertBoosted(knight);
    }

    @Test
    void getsBoostWhenOpponentHasThreeMoreCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent knight = addKnight();

        assertBoosted(knight);
    }

    @Test
    void getsBoostWhenOpponentHasThreeMoreCardsInHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent knight = addKnight();

        assertBoosted(knight);
    }

    @Test
    void remembersBeingWayBehindEarlierThisTurn() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.inMutationScope(() -> {});
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent knight = addKnight();

        assertBoosted(knight);
    }

    @Test
    void staysUnboostedWhenNoWayBehindThresholdIsMet() {
        Permanent knight = addKnight();

        assertUnboosted(knight);
    }

    @Test
    void staysUnboostedJustBelowEveryThreshold() {
        Permanent knight = addKnight();
        harness.setLife(player1, 11);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.inMutationScope(() -> {});

        assertUnboosted(knight);
    }

    @Test
    void remembersHandDisadvantageBeforeEnteringBattlefield() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.inMutationScope(() -> {});
        harness.setHand(player2, List.of());

        assertBoosted(addKnight());
    }

    @Test
    void losesHistoricalBonusWhenTheNextTurnBegins() {
        Permanent knight = addKnight();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.inMutationScope(() -> {});
        harness.setLife(player1, 20);
        assertBoosted(knight);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertUnboosted(knight);
    }

    @Test
    void remembersOpponentAnimatedLandAsACreatureBeforeEnteringBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new MishrasFactory());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 2, 0, null, null);
        harness.passBothPriorities();

        assertBoosted(addKnight());
    }

    @Test
    void countsControllersAnimatedLandWhenRecordingCreatureDisadvantage() {
        harness.addToBattlefield(player1, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.inMutationScope(() -> {});

        assertUnboosted(addKnight());
    }

    private void assertUnboosted(Permanent knight) {
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addKnight() {
        return harness.addToBattlefieldAndReturn(player1, new KnightOfLostCauses());
    }

    private void assertBoosted(Permanent knight) {
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
