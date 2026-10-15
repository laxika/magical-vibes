package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaktamunShinesAgain.class, GrizzlyBears.class, HillGiant.class})
class NaktamunShinesAgainTest extends BaseCardTest {

    @Test
    void chapterIPerpetuallyBoostsOwnedLowManaValueCreatureCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(0);

        triggerChapter();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(gd.perpetualCardPowerModifiers).containsEntry(bears.getCard().getId(), 1);

        bears.resetModifiers();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void chapterIISeeksRandomMatchingCreatureOntoTheBattlefield() {
        harness.setLibrary(player1, List.of(new HillGiant(), new GrizzlyBears()));
        addSaga(1);

        triggerChapter();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hill Giant");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
    }

    @Test
    void chapterIIIGrantsFlyingToYourLowManaValueCreaturesUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addSaga(2);

        triggerChapter();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new NaktamunShinesAgain());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    @Test
    void chapterIDoesNotBoostOrdinaryCreatureTokens() {
        GrizzlyBears tokenCopy = new GrizzlyBears();
        tokenCopy.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCopy);
        addSaga(0);

        triggerChapter();

        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(gd.perpetualCardPowerModifiers).doesNotContainKey(tokenCopy.getId());
    }

    @Test
    void chapterIBoostsCardsInEveryZoneAndFollowsOwnershipRatherThanControl() {
        GrizzlyBears hand = new GrizzlyBears();
        GrizzlyBears library = new GrizzlyBears();
        GrizzlyBears graveyard = new GrizzlyBears();
        GrizzlyBears exile = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setHand(player1, List.of(hand, giant));
        harness.setLibrary(player1, List.of(library));
        harness.setGraveyard(player1, List.of(graveyard));
        harness.setExile(player1, List.of(exile));
        GrizzlyBears owned = new GrizzlyBears();
        owned.setOwnerId(player1.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, owned);
        GrizzlyBears opposing = new GrizzlyBears();
        opposing.setOwnerId(player2.getId());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, opposing);
        addSaga(0);

        triggerChapter();

        for (GrizzlyBears card : List.of(hand, library, graveyard, exile, owned)) {
            assertThat(gd.perpetualCardPowerModifiers).containsEntry(card.getId(), 1);
        }
        assertThat(gd.perpetualCardPowerModifiers).doesNotContainKeys(giant.getId(), opposing.getId());
        assertThat(stolen.getEffectivePower()).isEqualTo(3);
        assertThat(borrowed.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void chapterIILeavesLibraryUnchangedWhenNoCreatureQualifies() {
        HillGiant giant = new HillGiant();
        NaktamunShinesAgain enchantment = new NaktamunShinesAgain();
        harness.setLibrary(player1, List.of(giant, enchantment));
        addSaga(1);

        triggerChapter();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(giant, enchantment);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void chapterIIAppliesThePerpetualBoostWhenSoughtCreatureEnters() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSaga(0);
        triggerChapter();

        triggerChapter();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst().orElseThrow();
        assertThat(bears.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void chapterIIIFlyingExcludesOpponentsAndLaterEntrantsAndExpires() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(2);

        triggerChapter();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.FLYING)).isFalse();
        harness.assertNotOnBattlefield(player1, "Naktamun Shines Again");
        harness.assertInGraveyard(player1, "Naktamun Shines Again");
        Permanent later = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, later, Keyword.FLYING)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
