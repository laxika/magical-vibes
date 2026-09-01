package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({Epochrasite.class, WrathOfGod.class})
class EpochrasiteTest extends BaseCardTest {

    @Test
    void entersWithCountersWhenNotCastFromHand() {
        Permanent epochrasite = harness.enterBattlefieldAndReturn(player1, new Epochrasite());

        assertThat(epochrasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void castFromHandDoesNotEnterWithCounters() {
        harness.setHand(player1, List.of(new Epochrasite()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent epochrasite = findPermanent(player1, "Epochrasite");
        assertThat(epochrasite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathExilesItWithSuspendAndReturnsItWithCountersAndHaste() {
        Permanent epochrasite = harness.enterBattlefieldAndReturn(player1, new Epochrasite());
        Card epochrasiteCard = epochrasite.getCard();
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
