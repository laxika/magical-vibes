package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongstalkBrawl.class, GrizzlyBears.class})
class LongstalkBrawlTest extends BaseCardTest {

    @Test
    void withoutGiftTheCreaturesFightWithoutACounterOrFish() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(ownBear.getId(), opponentBear.getId()), false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Fish");
    }

    @Test
    void withGiftPutsACounterBeforeTheFightAndCreatesATappedFish() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(ownBear.getId(), opponentBear.getId()), true);

        Permanent resolvedBear = findPermanent(player1, "Grizzly Bears");
        assertThat(resolvedBear).isNotNull();
        assertThat(resolvedBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(resolvedBear.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish).isNotNull();
        assertThat(fish.isTapped()).isTrue();
    }

    @Test
    void firstTargetMustBeACreatureYouControl() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LongstalkBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, List.of(opponentBear.getId(), opponentBear.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds, boolean giftPromised) {
        harness.setHand(player1, List.of(new LongstalkBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorceryWithGift(player1, 0, targetIds, giftPromised);
        harness.passBothPriorities();
    }
}
