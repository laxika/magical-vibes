package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarlordsFury.class, BalothGorger.class})
class WarlordsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Warlord's Fury grants first strike to all controlled creatures and draws a card")
    void grantsFirstStrikeAndDrawsCard() {
        Permanent bear1 = addCreatureReady(player1, new BalothGorger());
        Permanent bear2 = addCreatureReady(player1, new BalothGorger());
        harness.castFromHand(player1, new WarlordsFury(), "{R}");
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(bear1.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bear2.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Warlord's Fury does not grant first strike to opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        Permanent ownBear = addCreatureReady(player1, new BalothGorger());
        Permanent opponentBear = addCreatureReady(player2, new BalothGorger());
        harness.castFromHand(player1, new WarlordsFury(), "{R}");
        harness.passBothPriorities();

        assertThat(ownBear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opponentBear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Warlord's Fury first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new BalothGorger());
        harness.castFromHand(player1, new WarlordsFury(), "{R}");
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Warlord's Fury draws a card even with no creatures on the battlefield")
    void drawsCardWithNoCreatures() {
        harness.castFromHand(player1, new WarlordsFury(), "{R}");
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Creatures entering before resolution gain first strike, but later creatures do not")
    void affectsOnlyCreaturesPresentAtResolution() {
        harness.castFromHand(player1, new WarlordsFury(), "{R}");
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        assertThat(beforeResolution.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        assertThat(beforeResolution.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(afterResolution.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Warlord's Fury draws exactly the top card for its controller")
    void drawsOnlyForController() {
        BalothGorger drawnCard = new BalothGorger();
        WarlordsFury nextCard = new WarlordsFury();
        harness.setLibrary(player1, List.of(drawnCard, nextCard));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new WarlordsFury(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
    }
}
