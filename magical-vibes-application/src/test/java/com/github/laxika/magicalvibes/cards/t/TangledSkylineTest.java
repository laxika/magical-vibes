package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AtraxasFall;
import com.github.laxika.magicalvibes.cards.i.IchorDrinker;
import com.github.laxika.magicalvibes.cards.w.WaryThespian;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TangledSkyline.class, IchorDrinker.class, WaryThespian.class, AtraxasFall.class})
class TangledSkylineTest extends BaseCardTest {

    @Test
    void entersWithFiveLifeAndAnIncubatorWithFiveCounters() {
        harness.castFromHand(player1, new TangledSkyline(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(findPermanent(player1, "Incubator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void enterTriggerStillGainsLifeAndIncubatesAfterSkylineLeaves() {
        harness.castFromHand(player1, new TangledSkyline(), "{4}{G}");
        harness.passBothPriorities();
        Permanent skyline = findPermanent(player1, "Tangled Skyline");

        gd.playerBattlefields.get(player1.getId()).remove(skyline);
        harness.setGraveyard(player1, List.of(skyline.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Tangled Skyline");
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.REACH)).isFalse();
    }

    @Test
    void givesReachToPhyrexiansOnlyUnderItsControllersControl() {
        harness.addToBattlefield(player1, new TangledSkyline());
        Permanent ownPhyrexian = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        Permanent ownNonPhyrexian = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        Permanent opposingPhyrexian = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());

        assertThat(gqs.hasKeyword(gd, ownPhyrexian, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonPhyrexian, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingPhyrexian, Keyword.REACH)).isFalse();
    }

    @Test
    void incubatorTransformsOnOpponentsTurnAndGainsReachWithItsFiveCounters() {
        harness.castFromHand(player1, new TangledSkyline(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent incubator = findPermanent(player1, "Incubator");

        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.REACH)).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, incubator, Keyword.REACH)).isTrue();
        harness.assertLife(player1, 25);
    }

    @Test
    void skylineHasReachWhenItBecomesAPhyrexianCreature() {
        Permanent skyline = harness.addToBattlefieldAndReturn(player1, new TangledSkyline());
        skyline.setAnimatedUntilEndOfTurn(true);
        skyline.setAnimatedPower(4);
        skyline.setAnimatedToughness(4);
        skyline.getTransientSubtypes().add(CardSubtype.PHYREXIAN);

        assertThat(gqs.isCreature(gd, skyline)).isTrue();
        assertThat(gqs.hasKeyword(gd, skyline, Keyword.REACH)).isTrue();
    }

    @Test
    void reachEndsWhenSkylineLeavesTheBattlefield() {
        Permanent skyline = harness.addToBattlefieldAndReturn(player1, new TangledSkyline());
        Permanent phyrexian = harness.addToBattlefieldAndReturn(player1, new IchorDrinker());
        assertThat(gqs.hasKeyword(gd, phyrexian, Keyword.REACH)).isTrue();

        harness.setHand(player1, List.of(new AtraxasFall()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, skyline.getId());

        harness.assertNotOnBattlefield(player1, "Tangled Skyline");
        assertThat(gqs.hasKeyword(gd, phyrexian, Keyword.REACH)).isFalse();
    }
}
