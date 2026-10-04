package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FatalAttraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Epochrasite.class, FatalAttraction.class})
class EpochrasiteTest extends BaseCardTest {

    @Test
    void entersWithCountersWhenNotCastFromHand() {
        Permanent epochrasite = harness.enterBattlefieldAndReturn(player1, new Epochrasite());

        assertThat(epochrasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void castFromHandDoesNotEnterWithCounters() {
        harness.castFromHand(player1, new Epochrasite(), "{2}");
        harness.passBothPriorities();

        Permanent epochrasite = findPermanent(player1, "Epochrasite");
        assertThat(epochrasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathExilesItWithSuspendAndReturnsItWithCountersAndHaste() {
        Card epochrasiteCard = exileEpochrasiteWithFatalAttraction();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(epochrasiteCard);
        assertThat(gd.exiledCardTimeCounters).containsEntry(epochrasiteCard.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Epochrasite");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
    }

    @Test
    void suspendRemovesOneCounterOnlyDuringOwnersUpkeep() {
        Card epochrasiteCard = exileEpochrasiteWithFatalAttraction();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(epochrasiteCard.getId(), 3);

        for (int remaining = 2; remaining >= 1; remaining--) {
            advanceToUpkeep(player1);
            resolveAllTriggers();

            assertThat(gd.exiledCardTimeCounters).containsEntry(epochrasiteCard.getId(), remaining);
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(epochrasiteCard);
            assertThat(countPermanents(player1, "Epochrasite")).isZero();
        }
    }

    @Test
    void decliningSuspendCastLeavesItInExile() {
        Card epochrasiteCard = exileEpochrasiteWithFatalAttraction();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(epochrasiteCard);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(epochrasiteCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().equals(epochrasiteCard));
    }

    private Card exileEpochrasiteWithFatalAttraction() {
        Permanent epochrasite = addCreatureReady(player1, new Epochrasite());
        Card epochrasiteCard = epochrasite.getCard();
        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, epochrasite.getId());
        resolveAllTriggers();
        return epochrasiteCard;
    }
}
