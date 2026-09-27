package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TyrantGuard.class, Forest.class, GrizzlyBears.class})
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
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tyrant Guard");
        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, countered, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
