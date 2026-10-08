package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GenestealerPatriarch;
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

@CardUsed({TyrantGuard.class, Forest.class, GenestealerPatriarch.class})
class TyrantGuardTest extends BaseCardTest {

    @Test
    @DisplayName("At X=5, Tyrant Guard enters with counters and draws a card")
    void ravenousAtFive() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TyrantGuard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        gs.playCard(gd, player1, 0, 5, null, null);
        resolveAllTriggers();

        Permanent tyrantGuard = findPermanent(player1, "Tyrant Guard");
        assertThat(tyrantGuard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("At X=4, Tyrant Guard enters with counters without drawing")
    void ravenousBelowFiveDoesNotDraw() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TyrantGuard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 4, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Tyrant Guard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing Tyrant Guard protects creatures with counters until end of turn")
    void sacrificeGrantsKeywordsToCounteredCreaturesUntilEndOfTurn() {
        addCreatureReady(player1, new TyrantGuard());
        Permanent countered = addCreatureReady(player1, new GenestealerPatriarch());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncountered = addCreatureReady(player1, new GenestealerPatriarch());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tyrant Guard");
        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void ravenousBelowFiveDoesNotPutADrawTriggerOnTheStack() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TyrantGuard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 4, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tyrant Guard");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void ravenousAtZeroEntersWithoutCountersOrDrawing() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TyrantGuard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Tyrant Guard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastUsesZeroForRavenous() {
        harness.setLibrary(player1, List.of(new Forest()));

        Permanent guard = harness.enterBattlefieldAndReturn(player1, new TyrantGuard());

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void ravenousAboveFiveStillDrawsOneAfterSourceIsSacrificed() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TyrantGuard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        gs.playCard(gd, player1, 0, 6, null, null);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Tyrant Guard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Tyrant Guard");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void shieldwallUsesCountersAtResolutionAndOnlyProtectsOwnCreatures() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new TyrantGuard());
        guard.tap();
        Permanent gainingCounter = addCreatureReady(player1, new GenestealerPatriarch());
        Permanent losingCounter = addCreatureReady(player1, new GenestealerPatriarch());
        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opponent = addCreatureReady(player2, new GenestealerPatriarch());
        opponent.setCounterCount(CounterType.CHARGE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Tyrant Guard");
        assertThat(gqs.hasKeyword(gd, gainingCounter, Keyword.HEXPROOF)).isFalse();
        gainingCounter.setCounterCount(CounterType.CHARGE, 1);
        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, gainingCounter, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, gainingCounter, Keyword.INDESTRUCTIBLE)).isTrue();
        for (Permanent excluded : List.of(losingCounter, opponent, land)) {
            assertThat(gqs.hasKeyword(gd, excluded, Keyword.HEXPROOF)).isFalse();
            assertThat(gqs.hasKeyword(gd, excluded, Keyword.INDESTRUCTIBLE)).isFalse();
        }
    }

    @Test
    void shieldwallLocksInProtectedCreaturesWhenItResolves() {
        addCreatureReady(player1, new TyrantGuard());
        Permanent protectedCreature = addCreatureReady(player1, new GenestealerPatriarch());
        protectedCreature.setCounterCount(CounterType.CHARGE, 1);
        Permanent initiallyUncountered = addCreatureReady(player1, new GenestealerPatriarch());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        protectedCreature.setCounterCount(CounterType.CHARGE, 0);
        initiallyUncountered.setCounterCount(CounterType.CHARGE, 1);
        Permanent newcomer = addCreatureReady(player1, new GenestealerPatriarch());
        newcomer.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        for (Permanent excluded : List.of(initiallyUncountered, newcomer)) {
            assertThat(gqs.hasKeyword(gd, excluded, Keyword.HEXPROOF)).isFalse();
            assertThat(gqs.hasKeyword(gd, excluded, Keyword.INDESTRUCTIBLE)).isFalse();
        }
    }
}
