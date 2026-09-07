package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarOfTheResolute.class, GrizzlyBears.class})
class AvatarOfTheResoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each other countered creature you control")
    void entersWithCountersForControlledCounteredCreatures() {
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent secondCounteredCreature = addCreatureReady(player1, new GrizzlyBears());
        secondCounteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());

        Permanent avatar = castAvatar();

        assertThat(avatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count an opposing creature with a +1/+1 counter")
    void ignoresOpposingCounteredCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent avatar = castAvatar();

        assertThat(avatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count a creature you control without a +1/+1 counter")
    void ignoresUncounteredCreature() {
        Permanent avatar = castAvatarWithCreature();

        assertThat(avatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castAvatar() {
        harness.setHand(player1, List.of(new AvatarOfTheResolute()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Avatar of the Resolute");
    }

    private Permanent castAvatarWithCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        return castAvatar();
    }
}
