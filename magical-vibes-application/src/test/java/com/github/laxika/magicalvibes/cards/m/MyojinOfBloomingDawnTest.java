package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfBloomingDawn.class, GrizzlyBears.class, Forest.class})
class MyojinOfBloomingDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with a indestructible counter and indestructible")
    void castFromHandEntersWithIndestructibleCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MyojinOfBloomingDawn(), "{5}{W}{W}{W}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Blooming Dawn");
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get a indestructible counter")
    void enteringWithoutCastingDoesNotGetIndestructibleCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfBloomingDawn());

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing a indestructible counter creates one Spirit token per permanent you control")
    void removingIndestructibleCounterCreatesSpiritTokensPerPermanent() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
        assertThat(findPermanents(player1, "Spirit"))
                .allMatch(spirit -> spirit.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without a indestructible counter")
    void cannotActivateWithoutIndestructibleCounter() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("The counter is paid immediately, before any Spirits are created")
    void counterIsRemovedAsActivationCost() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Myojin can activate during an opponent's turn")
    void tappedSummoningSickMyojinCanActivateOnOpponentsTurn() {
        Permanent myojin = harness.addToBattlefieldAndReturn(player1, new MyojinOfBloomingDawn());
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        myojin.setSummoningSick(true);
        myojin.setTapped(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Each resolution counts current permanents, including tokens from an earlier resolution")
    void stackedActivationsCountPermanentsAtResolution() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new Forest());
        resolveAllTriggers();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).hasSize(6);
        assertThat(findPermanents(player1, "Spirit")).allSatisfy(spirit -> {
            assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
            assertThat(spirit.getCard().getColors()).isEmpty();
            assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        });
    }

    private Permanent addReadyMyojin() {
        return addCreatureReady(player1, new MyojinOfBloomingDawn());
    }
}
