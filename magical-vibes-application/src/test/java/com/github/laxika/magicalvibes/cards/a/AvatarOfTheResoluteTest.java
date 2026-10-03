package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarOfTheResolute.class, ColossodonYearling.class, AtarkaMonument.class})
class AvatarOfTheResoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each other countered creature you control")
    void entersWithCountersForControlledCounteredCreatures() {
        Permanent counteredCreature = addCreatureReady(player1, new ColossodonYearling());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent secondCounteredCreature = addCreatureReady(player1, new ColossodonYearling());
        secondCounteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new ColossodonYearling());

        Permanent avatar = castAvatar();

        assertThat(avatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count an opposing creature with a +1/+1 counter")
    void ignoresOpposingCounteredCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new ColossodonYearling());
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
        harness.castFromHand(player1, new AvatarOfTheResolute(), "{G}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Avatar of the Resolute");
    }

    private Permanent castAvatarWithCreature() {
        addCreatureReady(player1, new ColossodonYearling());
        return castAvatar();
    }

    @Test
    void countsCreaturesRatherThanIndividualCounters() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        assertThat(castAvatar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ignoresCreaturesWithOnlyOtherCounterTypes() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThat(castAvatar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void ignoresNoncreaturePermanentsWithPlusOnePlusOneCounters() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new AtarkaMonument());
        monument.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(castAvatar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsCountersPresentAtEntryRatherThanAtCasting() {
        Permanent creature = addCreatureReady(player1, new ColossodonYearling());
        harness.castFromHand(player1, new AvatarOfTheResolute(), "{G}{G}");
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        Permanent avatar = findPermanent(player1, "Avatar of the Resolute");
        assertThat(avatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(avatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
