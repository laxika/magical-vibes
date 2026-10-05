package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfRoaringBlades.class, AncientBrontodon.class, Forest.class})
class MyojinOfRoaringBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with an indestructible counter and indestructible")
    void castFromHandEntersWithIndestructibleCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MyojinOfRoaringBlades(), "{5}{R}{R}{R}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Roaring Blades");
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get an indestructible counter")
    void enteringWithoutCastingDoesNotGetIndestructibleCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfRoaringBlades());

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the counter deals 7 damage to each of up to three targets")
    void removingCounterDealsDamageToEachTarget() {
        Permanent myojin = addReadyMyojin();
        Permanent brontodon = addCreatureReady(player2, new AncientBrontodon());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbilityWithMultiTargets(
                player1,
                0,
                0,
                List.of(player1.getId(), player2.getId(), brontodon.getId()));
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(brontodon.getMarkedDamage()).isEqualTo(7);
    }

    @Test
    @DisplayName("The ability cannot target a land")
    void cannotTargetLand() {
        Permanent myojin = addReadyMyojin();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void canActivateWithZeroTargets() {
        Permanent myojin = addReadyMyojin();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWithoutIndestructibleCounter() {
        harness.addToBattlefield(player1, new MyojinOfRoaringBlades());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotChooseSameTargetTwice() {
        Permanent myojin = addReadyMyojin();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void cannotChooseMoreThanThreeTargets() {
        Permanent myojin = addReadyMyojin();
        Permanent brontodon = addCreatureReady(player2, new AncientBrontodon());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0,
                List.of(player1.getId(), player2.getId(), myojin.getId(), brontodon.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent myojin = addReadyMyojin();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId()));
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(myojin);
        gd.playerGraveyards.get(player1.getId()).add(myojin.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
    }

    @Test
    void remainingTargetStillTakesSevenDamageWhenOtherTargetLeaves() {
        addReadyMyojin();
        Permanent brontodon = addCreatureReady(player2, new AncientBrontodon());
        harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), brontodon.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(brontodon);
        gd.playerGraveyards.get(player2.getId()).add(brontodon.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
    }

    private Permanent addReadyMyojin() {
        Permanent myojin = addCreatureReady(player1, new MyojinOfRoaringBlades());
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        return myojin;
    }
}
