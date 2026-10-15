package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BumiEclecticEarthbender.class, Forest.class, GrizzlyBears.class})
class BumiEclecticEarthbenderTest extends BaseCardTest {

    @Test
    void entersByEarthbendingALandYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBumi(land);

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void attackingCountersEveryOwnLandCreatureButNoOrdinaryLandOrOpposingLandCreature() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ordinaryLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent firstBumi = castBumi(firstLand);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, firstBumi));
        resolveAllTriggers();
        Permanent bumi = castBumi(secondLand);

        harness.setHand(player2, List.of(new BumiEclecticEarthbender()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, 0, opposingLand.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        bumi.setSummoningSick(false);
        declareAttackers(List.of(indexOf(player1, bumi)));
        resolveAllTriggers();

        assertThat(firstLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(secondLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ordinaryLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void earthbendedLandReturnsTappedAfterDyingEvenAfterBumiLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bumi = castBumi(land);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, bumi));
        resolveAllTriggers();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, land));
        harness.assertInGraveyard(player1, "Forest");
        resolveAllTriggers();

        assertReturnedLand(land);
    }

    @Test
    void earthbendedLandReturnsTappedAfterExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBumi(land);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, land));
        harness.assertNotOnBattlefield(player1, "Forest");
        resolveAllTriggers();

        assertReturnedLand(land);
    }

    private void assertReturnedLand(Permanent original) {
        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getCard().getId()).isEqualTo(original.getCard().getId());
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void attackingPutsTwoCountersOnEachLandCreatureYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bumi = castBumi(land);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        bumi.setSummoningSick(false);

        declareAttackers(List.of(indexOf(player1, bumi)));
        resolveAllTriggers();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castBumi(Permanent targetLand) {
        harness.setHand(player1, List.of(new BumiEclecticEarthbender()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetLand.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Bumi, Eclectic Earthbender");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
