package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArrestersAdmonition;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
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

@CardUsed({GallopingLizrog.class, Forest.class, GrizzlyBears.class, SauroformHybrid.class,
        ArrestersAdmonition.class, Solemnity.class})
class GallopingLizrogTest extends BaseCardTest {

    @Test
    @DisplayName("ETB removes chosen counters from controlled creatures and doubles them on itself")
    void removesChosenCountersAndDoublesThem() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        firstCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        secondCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        cast();
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        Permanent lizrog = findPermanent(player1, "Galloping Lizrog");
        assertThat(lizrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Choosing zero counters leaves the battlefield unchanged")
    void choosingZeroCountersDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast();
        harness.handleListChoice(player1, "0");

        Permanent lizrog = findPermanent(player1, "Galloping Lizrog");
        assertThat(lizrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB resolves without a choice when no controlled creature has counters")
    void noEligibleCountersNeedsNoChoice() {
        cast();

        assertThat(findPermanent(player1, "Galloping Lizrog")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Lizrog can remove its own existing counters and double only those removed")
    void canRemoveCountersFromItself() {
        harness.castFromHand(player1, new GallopingLizrog(), "{3}{G}{U}");
        harness.passBothPriorities();
        Permanent lizrog = findPermanent(player1, "Galloping Lizrog");
        lizrog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");

        assertThat(lizrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Counters may still be removed after Lizrog leaves before its trigger resolves")
    void canRemoveCountersAfterLizrogLeaves() {
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        donor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.castFromHand(player1, new GallopingLizrog(), "{3}{G}{U}");
        harness.passBothPriorities();
        Permanent lizrog = findPermanent(player1, "Galloping Lizrog");

        harness.setHand(player2, List.of(new ArrestersAdmonition()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, lizrog.getId());
        harness.assertNotOnBattlefield(player1, "Galloping Lizrog");
        harness.assertInHand(player1, "Galloping Lizrog");

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "2");

        assertThat(donor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Solemnity prevents placement but does not prevent choosing to remove counters")
    void canRemoveCountersWhenPlacementIsForbidden() {
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        donor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player2, new Solemnity());

        cast();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "2");

        assertThat(donor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Galloping Lizrog")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast() {
        harness.castFromHand(player1, new GallopingLizrog(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
