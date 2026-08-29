package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GnarlidColonyTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotEnterWithCountersOrTrample() {
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent colony = findPermanent(player1, "Gnarlid Colony");
        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, colony, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void castWithKickerEntersWithTwoCountersAndTrample() {
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent colony = findPermanent(player1, "Gnarlid Colony");
        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, colony, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void onlyControlledCreaturesWithPlusOneCountersGetTrample() {
        harness.addToBattlefield(player1, new GnarlidColony());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        Permanent opposingBear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        ownBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.TRAMPLE)).isFalse();
    }
}
